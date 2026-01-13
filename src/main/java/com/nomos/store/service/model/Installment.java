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

    @Column(name = "paid_penalty", nullable = false)
    @Builder.Default
    private Double paidPenalty = 0.0;

    @Column(name = "capital_amount")
    private Double capitalAmount;

    @Column(name = "interest_amount")
    private Double interestAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InstallmentStatus status;


    /**
     * Calcula la Mora Total Acumulada a la fecha.
     */
    public Double getCalculatedPenalty() {
        LocalDate calculationDate = LocalDate.now();

        if (this.dueDate.isAfter(calculationDate)) {
            return 0.0;
        }

        long daysLate = ChronoUnit.DAYS.between(this.dueDate, calculationDate);
        if (daysLate <= 0) return 0.0;

        double capitalPending = this.expectedAmount - this.paidAmount;

        double penalty = capitalPending * 0.009 * (daysLate / 30.0);

        return Math.round(penalty * 100.0) / 100.0;
    }

    /**
     * Devuelve cuánto falta pagar de mora.
     */
    public Double getPendingPenalty() {
        double totalPenalty = getCalculatedPenalty();

        return Math.max(0.0, totalPenalty);
    }

    /**
     * Getter virtual para que el JSON muestre la mora actual al frontend
     */
    public Double getPenaltyAmount() {
        return getCalculatedPenalty();
    }

    /**
     * Lógica de imputación de pagos: Primero Mora, Luego Capital.
     */
    public Double applyPaymentLogic(Double availableAmount) {
        if (availableAmount <= 0) return 0.0;

        double penaltyDue = getPendingPenalty();
        if (penaltyDue > 0) {
            double paymentToPenalty = Math.min(availableAmount, penaltyDue);
            this.paidPenalty += paymentToPenalty;
            availableAmount -= paymentToPenalty;
        }

        if (availableAmount <= 0.001) {
            updateStatus();
            return 0.0;
        }

        double capitalDue = this.expectedAmount - this.paidAmount;
        if (capitalDue > 0) {
            double paymentToCapital = Math.min(availableAmount, capitalDue);
            this.paidAmount += paymentToCapital;
            availableAmount -= paymentToCapital;
        }

        updateStatus();
        return availableAmount;
    }


    public Double getPendingAmount() {
        if (expectedAmount == null) return 0.0;
        return expectedAmount - (paidAmount != null ? paidAmount : 0.0);
    }

    public boolean isFullyPaid() {
        return paidAmount >= expectedAmount - 0.01;
    }

    /**
     * ESTE ES EL MÉTODO QUE FALTABA O ESTABA MAL DEFINIDO
     * Verifica si la cuota está vencida respecto a una fecha dada.
     */
    public boolean isOverdue(LocalDate referenceDate) {
        if (this.status == InstallmentStatus.PAID) return false;

        return this.status == InstallmentStatus.OVERDUE || referenceDate.isAfter(dueDate);
    }

    private void updateStatus() {

        boolean capitalPaid = this.paidAmount >= this.expectedAmount - 0.01;
        boolean penaltyPaid = getPendingPenalty() <= 0.01;

        if (capitalPaid && penaltyPaid) {
            this.status = InstallmentStatus.PAID;
        } else if (this.paidAmount > 0 || this.paidPenalty > 0) {
            this.status = InstallmentStatus.PARTIAL;
        } else {
            if (this.dueDate.isBefore(LocalDate.now())) {
                this.status = InstallmentStatus.OVERDUE;
            } else {
                this.status = InstallmentStatus.PENDING;
            }
        }
    }
}