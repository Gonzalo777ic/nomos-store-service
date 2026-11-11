package com.valeerp.payable;

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
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "accounts_payable_installments")
public class PayableInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_payable_id", nullable = false)
    private AccountsPayable accountsPayable;

    @Column(name = "installment_number", nullable = false)
    private Integer number;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal expectedAmount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PayableInstallmentStatus status;

    protected PayableInstallment() {
    }

    static PayableInstallment schedule(int number, LocalDate dueDate, Money expected) {
        if (number <= 0) {
            throw new DomainException("El número de cuota CxP debe ser positivo");
        }
        if (dueDate == null) {
            throw new DomainException("El vencimiento de la cuota CxP es obligatorio");
        }
        if (expected == null || !expected.isPositive()) {
            throw new DomainException("El monto esperado de la cuota CxP debe ser positivo");
        }
        PayableInstallment installment = new PayableInstallment();
        installment.number = number;
        installment.dueDate = dueDate;
        installment.expectedAmount = expected.amount();
        installment.paidAmount = BigDecimal.ZERO;
        installment.currencyCode = expected.currency();
        installment.status = PayableInstallmentStatus.PENDING;
        return installment;
    }

    void attachTo(AccountsPayable payable) {
        this.accountsPayable = payable;
        if (payable != null) {
            this.currencyCode = payable.getCurrencyCode();
        }
    }

    Money applyPayment(Money available) {
        if (available == null || !available.isPositive()) {
            return available == null ? Money.zero(currencyCode) : available;
        }
        Money due = expected().minus(paid());
        if (!due.isPositive()) {
            refreshStatus();
            return available;
        }
        Money applied = available.cappedAt(due);
        this.paidAmount = this.paidAmount.add(applied.amount());
        refreshStatus();
        return available.minus(applied);
    }

    public Money expected() {
        return Money.of(expectedAmount, currencyCode);
    }

    public Money paid() {
        return Money.of(paidAmount == null ? BigDecimal.ZERO : paidAmount, currencyCode);
    }

    private void refreshStatus() {
        BigDecimal tolerance = new BigDecimal("0.01");
        if (paid().amount().compareTo(expectedAmount.subtract(tolerance)) >= 0) {
            this.status = PayableInstallmentStatus.PAID;
        } else if (paidAmount.signum() > 0) {
            this.status = PayableInstallmentStatus.PARTIAL;
        } else if (dueDate.isBefore(LocalDate.now())) {
            this.status = PayableInstallmentStatus.OVERDUE;
        } else {
            this.status = PayableInstallmentStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public PayableInstallmentStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PayableInstallment installment) || id == null) {
            return false;
        }
        return id.equals(installment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
