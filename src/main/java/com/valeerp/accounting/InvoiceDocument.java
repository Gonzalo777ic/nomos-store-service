package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Factura cliente / factura proveedor contable local (#72).
 * Propone asiento vía stub interno al publicar; libro: cancelación por estado/reverso, sin delete.
 */
@Entity
@Table(name = "accounting_invoice_documents")
public class InvoiceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InvoiceSide side;

    @Column(name = "document_number", nullable = false, length = 64)
    private String documentNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "tax_rate_id")
    private Long taxRateId;

    @Column(name = "untaxed_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal untaxedAmount;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "proposed_journal_entry_id")
    private Long proposedJournalEntryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InvoiceDocumentStatus status;

    /** Vista inversa; la persistencia de asignaciones la posee {@link Payment}. */
    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private List<PaymentAllocation> allocations = new ArrayList<>();

    protected InvoiceDocument() {
    }

    public static InvoiceDocument draft(Long companyId, Long partnerId, InvoiceSide side,
                                        String documentNumber, LocalDate issueDate, LocalDate dueDate,
                                        Long taxRateId, Money untaxed, Money tax, Money total) {
        if (companyId == null || partnerId == null) {
            throw new DomainException("La factura exige compañía y partner");
        }
        if (side == null) {
            throw new DomainException("El lado de la factura es obligatorio");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new DomainException("El número de factura es obligatorio");
        }
        if (issueDate == null) {
            throw new DomainException("La fecha de emisión es obligatoria");
        }
        if (untaxed == null || tax == null || total == null) {
            throw new DomainException("Los montos de la factura son obligatorios");
        }
        if (!untaxed.currency().equals(tax.currency()) || !tax.currency().equals(total.currency())) {
            throw new DomainException("Los montos de la factura deben compartir moneda");
        }
        if (!total.equals(untaxed.plus(tax))) {
            throw new DomainException("El total debe ser base + impuesto");
        }
        if (!total.isPositive()) {
            throw new DomainException("El total de la factura debe ser positivo");
        }
        InvoiceDocument invoice = new InvoiceDocument();
        invoice.companyId = companyId;
        invoice.partnerId = partnerId;
        invoice.side = side;
        invoice.documentNumber = documentNumber.trim();
        invoice.issueDate = issueDate;
        invoice.dueDate = dueDate;
        invoice.taxRateId = taxRateId;
        invoice.untaxedAmount = untaxed.amount();
        invoice.taxAmount = tax.amount();
        invoice.totalAmount = total.amount();
        invoice.currencyCode = total.currency();
        invoice.status = InvoiceDocumentStatus.DRAFT;
        return invoice;
    }

    /**
     * Publica la factura tras proponer el asiento (el caso de uso invoca el stub y pasa el id).
     */
    public void markPosted(Long journalEntryId) {
        if (status != InvoiceDocumentStatus.DRAFT) {
            throw new DomainException("Solo una factura en borrador se puede publicar");
        }
        if (journalEntryId == null) {
            throw new DomainException("La factura publicada exige asiento propuesto");
        }
        this.proposedJournalEntryId = journalEntryId;
        this.status = InvoiceDocumentStatus.POSTED;
    }

    public void cancel() {
        if (status == InvoiceDocumentStatus.CANCELLED) {
            throw new DomainException("La factura ya está cancelada");
        }
        if (status == InvoiceDocumentStatus.PAID || paidAmount().isPositive()) {
            throw new DomainException("No se puede cancelar una factura con pagos aplicados");
        }
        this.status = InvoiceDocumentStatus.CANCELLED;
    }

    public void linkReversalEntry(Long reversalEntryId) {
        if (status != InvoiceDocumentStatus.CANCELLED) {
            throw new DomainException("Solo una factura cancelada puede enlazar el contra-asiento");
        }
        if (reversalEntryId == null) {
            throw new DomainException("El contra-asiento es obligatorio");
        }
        this.proposedJournalEntryId = reversalEntryId;
    }

    public PaymentAllocation applyPayment(Payment payment, Money amount) {
        if (status == InvoiceDocumentStatus.CANCELLED) {
            throw new DomainException("No se puede conciliar una factura cancelada");
        }
        if (status == InvoiceDocumentStatus.DRAFT) {
            throw new DomainException("La factura debe estar publicada para conciliar");
        }
        if (payment == null) {
            throw new DomainException("El pago es obligatorio");
        }
        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new DomainException("Solo un pago confirmado se puede aplicar a facturas");
        }
        if (payment.getDirection().expectedInvoiceSide() != side) {
            throw new DomainException("La dirección del pago no corresponde al lado de la factura");
        }
        if (!Objects.equals(payment.getPartnerId(), partnerId)) {
            throw new DomainException("El partner del pago no coincide con la factura");
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("El monto a aplicar debe ser positivo");
        }
        if (!currencyCode.equals(amount.currency())) {
            throw new DomainException("La moneda del monto no coincide con la factura");
        }
        if (amount.greaterThan(residual())) {
            throw new DomainException("El monto supera el saldo pendiente de la factura");
        }

        PaymentAllocation allocation = PaymentAllocation.link(payment, this, amount);
        allocations.add(allocation);
        payment.registerAllocation(allocation);
        refreshStatus();
        return allocation;
    }

    /** Solo asignaciones cuyo pago no esté CANCELLED. */
    public Money paidAmount() {
        Money paid = Money.zero(currencyCode);
        for (PaymentAllocation allocation : allocations) {
            if (allocation.getPayment().getStatus() == PaymentStatus.CANCELLED) {
                continue;
            }
            paid = paid.plus(allocation.amount());
        }
        return paid;
    }

    public Money residual() {
        return total().minus(paidAmount());
    }

    public Money total() {
        return Money.of(totalAmount, currencyCode);
    }

    public Money untaxed() {
        return Money.of(untaxedAmount, currencyCode);
    }

    public Money tax() {
        return Money.of(taxAmount, currencyCode);
    }

    void refreshStatus() {
        if (status == InvoiceDocumentStatus.CANCELLED) {
            return;
        }
        if (residual().isZero()) {
            this.status = InvoiceDocumentStatus.PAID;
        } else if (status == InvoiceDocumentStatus.PAID) {
            this.status = InvoiceDocumentStatus.POSTED;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public InvoiceSide getSide() {
        return side;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getTaxRateId() {
        return taxRateId;
    }

    public Long getProposedJournalEntryId() {
        return proposedJournalEntryId;
    }

    public InvoiceDocumentStatus getStatus() {
        return status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public List<PaymentAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InvoiceDocument invoice) || id == null) {
            return false;
        }
        return id.equals(invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
