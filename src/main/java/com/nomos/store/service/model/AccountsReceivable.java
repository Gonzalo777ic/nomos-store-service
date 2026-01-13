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

    @OneToMany(mappedBy = "accountsReceivable", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<CreditDocument> creditDocuments = new ArrayList<>();


    public Double getPaidAmount() {

        if (collections == null) return 0.0;
        return collections.stream()
                .filter(c -> !"ANULADO".equals(c.getStatus()))
                .mapToDouble(Collection::getAmount)
                .sum();
    }

    public Double getBalance() {




        double paidCapital = installments.stream().mapToDouble(Installment::getPaidAmount).sum();
        return totalAmount - paidCapital;
    }

    public void applyPayment(Collection collection, Installment specificInstallment) {
        if (this.status == AccountsReceivableStatus.CANCELLED) {
            throw new IllegalStateException("No se puede pagar una cuenta cancelada");
        }

        this.collections.add(collection);
        collection.setAccountsReceivable(this);

        double amountRemaining = collection.getAmount();

        if (specificInstallment != null) {


            specificInstallment.applyPaymentLogic(amountRemaining);
        } else {

            List<Installment> pending = this.installments.stream()

                    .filter(i -> i.getStatus() != InstallmentStatus.PAID)
                    .sorted(Comparator.comparing(Installment::getDueDate))
                    .toList();

            for (Installment inst : pending) {
                if (amountRemaining <= 0.001) break;


                amountRemaining = inst.applyPaymentLogic(amountRemaining);
            }
        }
        updateStatus();
    }

    private void updateStatus() {

        boolean allPaid = installments.stream()
                .allMatch(i -> i.getStatus() == InstallmentStatus.PAID);

        if (allPaid) {
            this.status = AccountsReceivableStatus.PAID;
        } else {

            this.status = AccountsReceivableStatus.ACTIVE;
        }
    }
}