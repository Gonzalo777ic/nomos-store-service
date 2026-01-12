package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
     * Calcula cuánto falta por pagar en esta cuota.
     */
    public Double getPendingAmount() {
        if (expectedAmount == null) return 0.0;
        return expectedAmount - (paidAmount != null ? paidAmount : 0.0);
    }

    public boolean isFullyPaid() {
        return paidAmount >= expectedAmount - 0.01;
    }

    /**
     * Verifica vencimiento sin cambiar estado interno.
     */
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