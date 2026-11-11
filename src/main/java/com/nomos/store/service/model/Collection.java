package com.nomos.store.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.sales.SalesPolicy;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties({"details", "accountsReceivable"})
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_receivable_id", nullable = false)
    @JsonIgnore
    private AccountsReceivable accountsReceivable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    @JsonIgnore
    private Installment installment;

    @Column(name = "collection_date", nullable = false)
    private LocalDateTime collectionDate;

    @Column(nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = SalesPolicy.DEFAULT_CURRENCY;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodConfig paymentMethod;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CollectionStatus status;

    protected Collection() {
    }

    public static Collection confirm(Sale sale, AccountsReceivable ar, Money amount,
                                     PaymentMethodConfig paymentMethod, String referenceNumber,
                                     LocalDateTime collectionDate) {
        if (sale == null || ar == null) {
            throw new DomainException("El cobro exige venta y CxC");
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("El monto del cobro debe ser positivo");
        }
        if (paymentMethod == null) {
            throw new DomainException("El método de pago es obligatorio");
        }
        Collection collection = new Collection();
        collection.sale = sale;
        collection.accountsReceivable = ar;
        collection.amount = amount.amount();
        collection.currencyCode = amount.currency();
        collection.paymentMethod = paymentMethod;
        collection.referenceNumber = referenceNumber;
        collection.collectionDate = collectionDate == null ? LocalDateTime.now() : collectionDate;
        collection.status = CollectionStatus.CONFIRMED;
        return collection;
    }

    void attachTo(AccountsReceivable accountsReceivable) {
        this.accountsReceivable = accountsReceivable;
    }

    public void markConfirmed() {
        if (status == CollectionStatus.CONFIRMED) {
            throw new DomainException("La cobranza ya fue confirmada");
        }
        if (status == CollectionStatus.CANCELLED) {
            throw new DomainException("No se puede confirmar un cobro cancelado");
        }
        this.status = CollectionStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = CollectionStatus.CANCELLED;
    }

    public Money money() {
        return Money.of(amount, currencyCode);
    }

    @PrePersist
    public void prePersist() {
        if (this.collectionDate == null) {
            this.collectionDate = LocalDateTime.now();
        }
        if (this.currencyCode == null || this.currencyCode.isBlank()) {
            this.currencyCode = SalesPolicy.DEFAULT_CURRENCY;
        }
    }

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public AccountsReceivable getAccountsReceivable() {
        return accountsReceivable;
    }

    public Installment getInstallment() {
        return installment;
    }

    public LocalDateTime getCollectionDate() {
        return collectionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public PaymentMethodConfig getPaymentMethod() {
        return paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public CollectionStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Collection collection) || id == null) {
            return false;
        }
        return id.equals(collection.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
