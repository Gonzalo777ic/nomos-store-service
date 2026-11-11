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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Línea de asiento. Debe o haber en Money; nunca ambos positivos.
 */
@Entity
@Table(name = "accounting_journal_entry_lines")
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name = "account_code", nullable = false, length = 32)
    private String accountCode;

    @Column(name = "account_name", nullable = false, length = 160)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_class", nullable = false, length = 32)
    private AccountClass accountClass;

    @Column(name = "debit_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal creditAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(length = 240)
    private String memo;

    protected JournalEntryLine() {
    }

    static JournalEntryLine of(JournalEntry entry, String accountCode, String accountName,
                               AccountClass accountClass, Money debit, Money credit, String memo) {
        if (entry == null) {
            throw new DomainException("La línea exige asiento");
        }
        if (accountCode == null || accountCode.isBlank()) {
            throw new DomainException("El código de cuenta es obligatorio");
        }
        if (accountName == null || accountName.isBlank()) {
            throw new DomainException("El nombre de cuenta es obligatorio");
        }
        if (accountClass == null) {
            throw new DomainException("La clase de cuenta es obligatoria");
        }
        if (debit == null || credit == null) {
            throw new DomainException("Debe y haber son obligatorios");
        }
        if (!debit.currency().equals(credit.currency())) {
            throw new DomainException("Debe y haber deben compartir moneda");
        }
        if (debit.isNegative() || credit.isNegative()) {
            throw new DomainException("Debe y haber no pueden ser negativos");
        }
        if (debit.isZero() == credit.isZero()) {
            throw new DomainException("Cada línea debe tener debe o haber, no ambos ni ninguno");
        }
        JournalEntryLine line = new JournalEntryLine();
        line.journalEntry = entry;
        line.accountCode = accountCode.trim();
        line.accountName = accountName.trim();
        line.accountClass = accountClass;
        line.debitAmount = debit.amount();
        line.creditAmount = credit.amount();
        line.currencyCode = debit.currency();
        line.memo = blankToNull(memo);
        return line;
    }

    public Long getId() {
        return id;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public AccountClass getAccountClass() {
        return accountClass;
    }

    public Money debit() {
        return Money.of(debitAmount, currencyCode);
    }

    public Money credit() {
        return Money.of(creditAmount, currencyCode);
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getMemo() {
        return memo;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JournalEntryLine line) || id == null) {
            return false;
        }
        return id.equals(line.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
