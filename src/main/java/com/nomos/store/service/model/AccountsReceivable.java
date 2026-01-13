package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "accounts_receivable")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsReceivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false, unique = true)
    @JsonIgnoreProperties("accountsReceivable")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale sale;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @OneToMany(mappedBy = "accountsReceivable", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Installment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "accountsReceivable", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("accountsReceivable")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Collection> collections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountsReceivableStatus status;


    public Double getPaidAmount() {
        if (collections == null) return 0.0;
        return collections.stream()
                .filter(c -> !"ANULADO".equals(c.getStatus()))
                .mapToDouble(Collection::getAmount)
                .sum();
    }

    public Double getBalance() {
        return totalAmount - getPaidAmount();
    }

    public boolean hasOverdueInstallments() {
        if (installments == null) return false;
        LocalDate today = LocalDate.now();
        return installments.stream()
                .anyMatch(i -> i.isOverdue(today));
    }

    public void applyPayment(Collection collection, Installment specificInstallment) {
        if (this.status == AccountsReceivableStatus.CANCELLED) {
            throw new IllegalStateException("No se puede pagar una cuenta cancelada");
        }

        this.collections.add(collection);
        collection.setAccountsReceivable(this);

        double amountToDistribute = collection.getAmount();

        if (specificInstallment != null) {
            specificInstallment.addPayment(amountToDistribute);
        } else {
            List<Installment> pending = this.installments.stream()
                    .filter(i -> !i.isFullyPaid())
                    .sorted(Comparator.comparing(Installment::getDueDate))
                    .toList();
            for (Installment inst : pending) {
                if (amountToDistribute <= 0) break;

                double pendingAmount = inst.getPendingAmount();
                double payment = Math.min(pendingAmount, amountToDistribute);
                inst.addPayment(payment);
                amountToDistribute -= payment;
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        if (getBalance() <= 0.01) {
            this.status = AccountsReceivableStatus.PAID;
        } else {
            if (this.status != AccountsReceivableStatus.BAD_DEBT) {
                this.status = AccountsReceivableStatus.ACTIVE;
            }
        }
    }
}