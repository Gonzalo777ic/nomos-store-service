package com.valeerp.accounting;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Asiento contable (#71/#77). Libro: publicar y anular; nunca delete de asiento publicado.
 * La anulación marca REVERSED y produce un contra-asiento POSTED.
 */
@Entity
@Table(name = "accounting_journal_entries_v2")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "journal_id", nullable = false)
    private Long journalId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 240)
    private String concept;

    @Column(name = "reference_document", length = 80)
    private String referenceDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JournalEntryStatus status;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "reverses_entry_id")
    private Long reversesEntryId;

    @Column(name = "reversed_by_entry_id")
    private Long reversedByEntryId;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "voided_by", length = 120)
    private String voidedBy;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalEntryLine> lines = new ArrayList<>();

    protected JournalEntry() {
    }

    public static JournalEntry draft(Long companyId, Long journalId, LocalDate entryDate,
                                     String concept, String referenceDocument, String currencyCode) {
        if (companyId == null) {
            throw new DomainException("El asiento exige compañía");
        }
        if (journalId == null) {
            throw new DomainException("El asiento exige diario");
        }
        if (entryDate == null) {
            throw new DomainException("La fecha del asiento es obligatoria");
        }
        if (concept == null || concept.isBlank()) {
            throw new DomainException("El concepto del asiento es obligatorio");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new DomainException("La moneda del asiento es obligatoria");
        }
        JournalEntry entry = new JournalEntry();
        entry.companyId = companyId;
        entry.journalId = journalId;
        entry.entryDate = entryDate;
        entry.concept = concept.trim();
        entry.referenceDocument = blankToNull(referenceDocument);
        entry.status = JournalEntryStatus.DRAFT;
        entry.currencyCode = currencyCode.trim().toUpperCase();
        return entry;
    }

    public void addLine(String accountCode, String accountName, AccountClass accountClass,
                        Money debit, Money credit, String memo) {
        assertDraft();
        requireCurrency(debit);
        requireCurrency(credit);
        lines.add(JournalEntryLine.of(this, accountCode, accountName, accountClass, debit, credit, memo));
    }

    /**
     * Publica el asiento. {@code periodGuard} valida que el periodo de la fecha esté abierto
     * (bloquea diarios del periodo cerrado — #78).
     */
    public void post(Consumer<LocalDate> periodGuard, Instant when) {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("El asiento requiere al menos una línea");
        }
        assertBalanced();
        if (periodGuard != null) {
            periodGuard.accept(entryDate);
        }
        this.status = JournalEntryStatus.POSTED;
        this.postedAt = when;
    }

    /**
     * Anula un asiento publicado creando el contra-asiento. Nunca elimina filas del libro.
     *
     * @return contra-asiento en estado POSTED (listo para persistir junto al original)
     */
    public JournalEntry voidPosted(Consumer<LocalDate> periodGuard, Actor actor, Instant when, LocalDate reversalDate) {
        if (status != JournalEntryStatus.POSTED) {
            throw new DomainException("Solo se puede anular un asiento publicado");
        }
        if (reversedByEntryId != null) {
            throw new DomainException("El asiento ya fue anulado");
        }
        LocalDate effectiveDate = reversalDate == null ? entryDate : reversalDate;
        if (periodGuard != null) {
            periodGuard.accept(effectiveDate);
        }
        Actor resolved = actor == null ? Actor.SYSTEM : actor;

        JournalEntry reversal = draft(
                companyId,
                journalId,
                effectiveDate,
                "Reverso: " + concept,
                referenceDocument == null ? null : "REV-" + referenceDocument,
                currencyCode
        );
        reversal.reversesEntryId = this.id;
        for (JournalEntryLine line : lines) {
            reversal.lines.add(JournalEntryLine.of(
                    reversal,
                    line.getAccountCode(),
                    line.getAccountName(),
                    line.getAccountClass(),
                    line.credit(),
                    line.debit(),
                    line.getMemo()
            ));
        }
        reversal.status = JournalEntryStatus.POSTED;
        reversal.postedAt = when;

        this.status = JournalEntryStatus.REVERSED;
        this.voidedAt = when;
        this.voidedBy = resolved.id();
        return reversal;
    }

    void linkReversal(Long reversalEntryId) {
        this.reversedByEntryId = reversalEntryId;
    }

    public Money totalDebit() {
        Money total = Money.zero(currencyCode);
        for (JournalEntryLine line : lines) {
            total = total.plus(line.debit());
        }
        return total;
    }

    public Money totalCredit() {
        Money total = Money.zero(currencyCode);
        for (JournalEntryLine line : lines) {
            total = total.plus(line.credit());
        }
        return total;
    }

    public boolean isBalanced() {
        return totalDebit().equals(totalCredit());
    }

    private void assertBalanced() {
        if (!isBalanced()) {
            throw new DomainException(
                    "Asiento desbalanceado: debe " + totalDebit() + " != haber " + totalCredit());
        }
    }

    private void assertDraft() {
        if (status != JournalEntryStatus.DRAFT) {
            throw new DomainException("Solo un asiento en borrador admite esa operación");
        }
    }

    private void requireCurrency(Money money) {
        if (money == null) {
            throw new DomainException("El monto es obligatorio");
        }
        if (!currencyCode.equals(money.currency())) {
            throw new DomainException("La moneda de la línea no coincide con el asiento");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getJournalId() {
        return journalId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public String getConcept() {
        return concept;
    }

    public String getReferenceDocument() {
        return referenceDocument;
    }

    public JournalEntryStatus getStatus() {
        return status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Long getReversesEntryId() {
        return reversesEntryId;
    }

    public Long getReversedByEntryId() {
        return reversedByEntryId;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public Instant getVoidedAt() {
        return voidedAt;
    }

    public String getVoidedBy() {
        return voidedBy;
    }

    public List<JournalEntryLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JournalEntry entry) || id == null) {
            return false;
        }
        return id.equals(entry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
