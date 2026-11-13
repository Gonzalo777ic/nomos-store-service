package com.valeerp.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.sales.PenaltyPolicy;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "installments")
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_receivable_id", nullable = false)
    @JsonIgnore
    private AccountsReceivable accountsReceivable;

    @Column(name = "installment_number", nullable = false)
    private Integer number;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal expectedAmount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "paid_penalty", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal paidPenalty = BigDecimal.ZERO;

    @Column(name = "capital_amount", precision = 19, scale = Money.SCALE)
    private BigDecimal capitalAmount;

    @Column(name = "interest_amount", precision = 19, scale = Money.SCALE)
    private BigDecimal interestAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InstallmentStatus status;

    protected Installment() {
    }

    public static Installment schedule(int number, LocalDate dueDate, Money expected) {
        if (number <= 0) {
            throw new DomainException("El número de cuota debe ser positivo");
        }
        if (dueDate == null) {
            throw new DomainException("El vencimiento de la cuota es obligatorio");
        }
        if (expected == null || !expected.isPositive()) {
            throw new DomainException("El monto esperado de la cuota debe ser positivo");
        }
        Installment installment = new Installment();
        installment.number = number;
        installment.dueDate = dueDate;
        installment.expectedAmount = expected.amount();
        installment.paidAmount = BigDecimal.ZERO;
        installment.paidPenalty = BigDecimal.ZERO;
        installment.status = InstallmentStatus.PENDING;
        return installment;
    }

    void attachTo(AccountsReceivable accountsReceivable) {
        this.accountsReceivable = accountsReceivable;
    }

    public Money applyPayment(Money available) {
        if (available == null || !available.isPositive()) {
            return available == null ? Money.zero(currency()) : available;
        }
        Money remaining = available;
        Money penaltyDue = pendingPenalty();
        if (penaltyDue.isPositive()) {
            Money toPenalty = remaining.cappedAt(penaltyDue);
            this.paidPenalty = this.paidPenalty.add(toPenalty.amount());
            remaining = remaining.minus(toPenalty);
        }
        if (!remaining.isPositive()) {
            refreshStatus();
            return remaining;
        }
        Money capitalDue = expected().minus(paid());
        if (capitalDue.isPositive()) {
            Money toCapital = remaining.cappedAt(capitalDue);
            this.paidAmount = this.paidAmount.add(toCapital.amount());
            remaining = remaining.minus(toCapital);
        }
        refreshStatus();
        return remaining;
    }

    public Money reduceExpected(Money amount) {
        if (amount == null || !amount.isPositive()) {
            return amount == null ? Money.zero(currency()) : amount;
        }
        Money expected = expected();
        Money reduced = amount.cappedAt(expected);
        this.expectedAmount = expected.minus(reduced).amount();
        refreshStatus();
        return amount.minus(reduced);
    }

    public Money expected() {
        return Money.of(expectedAmount, currency());
    }

    public Money paid() {
        return Money.of(paidAmount == null ? BigDecimal.ZERO : paidAmount, currency());
    }

    public Money pendingAmount() {
        return expected().minus(paid());
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount().amount();
    }

    public Money pendingPenalty() {
        Money totalPenalty = calculatedPenalty();
        Money already = Money.of(paidPenalty == null ? BigDecimal.ZERO : paidPenalty, currency());
        Money pending = totalPenalty.minus(already);
        return pending.isNegative() ? Money.zero(currency()) : pending;
    }

    public Money calculatedPenalty() {
        LocalDate today = LocalDate.now();
        if (!dueDate.isBefore(today)) {
            return Money.zero(currency());
        }
        long daysLate = ChronoUnit.DAYS.between(dueDate, today);
        if (daysLate <= 0) {
            return Money.zero(currency());
        }
        BigDecimal capitalPending = pendingAmount().amount();
        BigDecimal penalty = capitalPending
                .multiply(BigDecimal.valueOf(PenaltyPolicy.monthlyRate()))
                .multiply(BigDecimal.valueOf(daysLate))
                .divide(BigDecimal.valueOf(30), Money.SCALE, RoundingMode.HALF_UP);
        return Money.of(penalty, currency());
    }

    public BigDecimal getCalculatedPenalty() {
        return calculatedPenalty().amount();
    }

    public BigDecimal getPendingPenalty() {
        return pendingPenalty().amount();
    }

    public BigDecimal getPenaltyAmount() {
        return getCalculatedPenalty();
    }

    public boolean isOverdue(LocalDate referenceDate) {
        if (status == InstallmentStatus.PAID) {
            return false;
        }
        return status == InstallmentStatus.OVERDUE || referenceDate.isAfter(dueDate);
    }

    private void refreshStatus() {
        BigDecimal tolerance = new BigDecimal("0.01");
        boolean capitalPaid = paid().amount().compareTo(expectedAmount.subtract(tolerance)) >= 0;
        boolean penaltyPaid = pendingPenalty().amount().compareTo(tolerance) <= 0;
        if (capitalPaid && penaltyPaid) {
            this.status = InstallmentStatus.PAID;
        } else if (paidAmount.signum() > 0 || paidPenalty.signum() > 0) {
            this.status = InstallmentStatus.PARTIAL;
        } else if (dueDate.isBefore(LocalDate.now())) {
            this.status = InstallmentStatus.OVERDUE;
        } else {
            this.status = InstallmentStatus.PENDING;
        }
    }

    private String currency() {
        return accountsReceivable == null ? com.valeerp.sales.SalesPolicy.DEFAULT_CURRENCY : accountsReceivable.getCurrencyCode();
    }

    public Long getId() {
        return id;
    }

    public AccountsReceivable getAccountsReceivable() {
        return accountsReceivable;
    }

    public Integer getNumber() {
        return number;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public BigDecimal getPaidPenalty() {
        return paidPenalty;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Installment installment) || id == null) {
            return false;
        }
        return id.equals(installment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
