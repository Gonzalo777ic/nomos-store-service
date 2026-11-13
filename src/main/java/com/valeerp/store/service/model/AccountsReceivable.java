package com.valeerp.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.sales.SalesPolicy;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Cuenta por cobrar (#74). Libro: sin delete; {@link #paidAmount()} excluye cobros CANCELLED.
 */
@Entity
@Table(name = "accounts_receivable")
public class AccountsReceivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false, unique = true)
    @JsonIgnoreProperties("accountsReceivable")
    private Sale sale;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = SalesPolicy.DEFAULT_CURRENCY;

    @OneToMany(mappedBy = "accountsReceivable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Installment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "accountsReceivable", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("accountsReceivable")
    private List<Collection> collections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountsReceivableStatus status;

    @OneToMany(mappedBy = "accountsReceivable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditDocument> creditDocuments = new ArrayList<>();

    protected AccountsReceivable() {
    }

    public static AccountsReceivable open(Sale sale, Money total) {
        if (sale == null) {
            throw new DomainException("La CxC exige venta");
        }
        if (total == null || !total.isPositive()) {
            throw new DomainException("El total de la CxC debe ser positivo");
        }
        AccountsReceivable ar = new AccountsReceivable();
        ar.sale = sale;
        ar.totalAmount = total.amount();
        ar.currencyCode = total.currency();
        ar.status = AccountsReceivableStatus.ACTIVE;
        return ar;
    }

    public void addInstallment(Installment installment) {
        if (installment == null) {
            throw new DomainException("La cuota es obligatoria");
        }
        installment.attachTo(this);
        installments.add(installment);
    }

    /** Solo cobros no cancelados. */
    public Money paidAmount() {
        Money paid = Money.zero(currencyCode);
        if (collections == null) {
            return paid;
        }
        for (Collection collection : collections) {
            if (collection.getStatus() == CollectionStatus.CANCELLED) {
                continue;
            }
            if (collection.getStatus() != CollectionStatus.CONFIRMED) {
                continue;
            }
            paid = paid.plus(collection.money());
        }
        return paid;
    }

    /** Compat JSON legado. */
    public BigDecimal getPaidAmount() {
        return paidAmount().amount();
    }

    public Money balance() {
        return total().minus(paidAmount());
    }

    public BigDecimal getBalance() {
        return balance().amount();
    }

    public Money total() {
        return Money.of(totalAmount, currencyCode);
    }

    public void applyCredit(Money amount) {
        if (status == AccountsReceivableStatus.CANCELLED) {
            throw new DomainException("No se puede acreditar una cuenta cancelada");
        }
        if (amount == null || !amount.isPositive()) {
            return;
        }
        Money reduced = total().minus(amount);
        if (reduced.isNegative()) {
            reduced = Money.zero(currencyCode);
        }
        this.totalAmount = reduced.amount();
        Money remaining = amount;
        List<Installment> pending = installments.stream()
                .filter(installment -> installment.getStatus() != InstallmentStatus.PAID)
                .sorted(Comparator.comparing(Installment::getDueDate).reversed())
                .toList();
        for (Installment installment : pending) {
            if (!remaining.isPositive()) {
                break;
            }
            remaining = installment.reduceExpected(remaining);
        }
        refreshStatus();
    }

    public void applyCredit(double amount) {
        applyCredit(Money.of(BigDecimal.valueOf(amount), currencyCode));
    }

    public void applyPayment(Collection collection, Installment specificInstallment) {
        if (status == AccountsReceivableStatus.CANCELLED) {
            throw new DomainException("No se puede pagar una cuenta cancelada");
        }
        if (collection == null) {
            throw new DomainException("El cobro es obligatorio");
        }
        if (collection.getStatus() == CollectionStatus.CANCELLED) {
            throw new DomainException("Un cobro cancelado no se imputa a la CxC");
        }
        collection.attachTo(this);
        if (!collections.contains(collection)) {
            collections.add(collection);
        }
        Money remaining = collection.money();
        if (specificInstallment != null) {
            specificInstallment.applyPayment(remaining);
        } else {
            List<Installment> pending = installments.stream()
                    .filter(i -> i.getStatus() != InstallmentStatus.PAID)
                    .sorted(Comparator.comparing(Installment::getDueDate))
                    .toList();
            for (Installment installment : pending) {
                if (!remaining.isPositive()) {
                    break;
                }
                remaining = installment.applyPayment(remaining);
            }
        }
        refreshStatus();
    }

    public void cancel() {
        this.status = AccountsReceivableStatus.CANCELLED;
        if (collections != null) {
            for (Collection collection : collections) {
                if (collection.getStatus() != CollectionStatus.CANCELLED) {
                    collection.cancel();
                }
            }
        }
    }

    private void refreshStatus() {
        if (status == AccountsReceivableStatus.CANCELLED) {
            return;
        }
        boolean allPaid = !installments.isEmpty()
                && installments.stream().allMatch(i -> i.getStatus() == InstallmentStatus.PAID);
        this.status = allPaid ? AccountsReceivableStatus.PAID : AccountsReceivableStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public List<Installment> getInstallments() {
        return Collections.unmodifiableList(installments);
    }

    public List<Collection> getCollections() {
        return Collections.unmodifiableList(collections);
    }

    public AccountsReceivableStatus getStatus() {
        return status;
    }

    public List<CreditDocument> getCreditDocuments() {
        return Collections.unmodifiableList(creditDocuments);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AccountsReceivable ar) || id == null) {
            return false;
        }
        return id.equals(ar.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
