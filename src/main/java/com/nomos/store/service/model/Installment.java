package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "installments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_receivable_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AccountsReceivable accountsReceivable;

    @Column(name = "installment_number", nullable = false)
    private Integer number;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "expected_amount", nullable = false)
    private Double expectedAmount;

    @Column(name = "paid_amount", nullable = false)
    @Builder.Default
    private Double paidAmount = 0.0;

    @Column(name = "capital_amount")
    private Double capitalAmount;

    @Column(name = "interest_amount")
    private Double interestAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InstallmentStatus status;


    /**
     * Calcula la mora al día de hoy según la regla:
     * Mora = SaldoVencido * 0.9% * (DíasAtraso / 30)
     */
    public Double getPenaltyAmount() {


        if (this.status == InstallmentStatus.PAID || this.dueDate.isAfter(LocalDate.now().minusDays(1))) {
            return 0.0;
        }

        long daysLate = ChronoUnit.DAYS.between(this.dueDate, LocalDate.now());

        if (daysLate <= 0) return 0.0;

        double pendingAmount = this.expectedAmount - (this.paidAmount != null ? this.paidAmount : 0.0);

        double penalty = pendingAmount * 0.009 * (daysLate / 30.0);

        return Math.round(penalty * 100.0) / 100.0;
    }


    public Double getPendingAmount() {
        if (expectedAmount == null) return 0.0;
        return expectedAmount - (paidAmount != null ? paidAmount : 0.0);
    }

    public boolean isFullyPaid() {
        return paidAmount >= expectedAmount - 0.01;
    }

    public boolean isOverdue(LocalDate referenceDate) {
        if (this.status == InstallmentStatus.PAID) return false;
        return this.status == InstallmentStatus.OVERDUE || referenceDate.isAfter(dueDate);
    }

    public void addPayment(Double amount) {
        this.paidAmount = (this.paidAmount == null ? 0.0 : this.paidAmount) + amount;
        updateStatus();
    }

    private void updateStatus() {
        if (isFullyPaid()) {
            this.status = InstallmentStatus.PAID;
        } else if (this.paidAmount > 0) {
            this.status = InstallmentStatus.PARTIAL;
        } else {
            this.status = InstallmentStatus.PENDING;
        }
    }
}