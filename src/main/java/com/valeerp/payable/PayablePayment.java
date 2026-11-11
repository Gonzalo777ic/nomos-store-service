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
@Table(name = "accounts_payable_payments")
public class PayablePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_payable_id", nullable = false)
    private AccountsPayable accountsPayable;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "reference_number", length = 80)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PayablePaymentStatus status;

    protected PayablePayment() {
    }

    static PayablePayment confirm(LocalDate paymentDate, Money amount, String referenceNumber) {
        if (paymentDate == null) {
            throw new DomainException("La fecha del pago CxP es obligatoria");
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("El monto del pago CxP debe ser positivo");
        }
        PayablePayment payment = new PayablePayment();
        payment.paymentDate = paymentDate;
        payment.amount = amount.amount();
        payment.currencyCode = amount.currency();
        payment.referenceNumber = referenceNumber == null || referenceNumber.isBlank() ? null : referenceNumber.trim();
        payment.status = PayablePaymentStatus.CONFIRMED;
        return payment;
    }

    void attachTo(AccountsPayable payable) {
        this.accountsPayable = payable;
    }

    public void cancel() {
        if (status == PayablePaymentStatus.CANCELLED) {
            throw new DomainException("El pago CxP ya está cancelado");
        }
        this.status = PayablePaymentStatus.CANCELLED;
    }

    public Money money() {
        return Money.of(amount, currencyCode);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public PayablePaymentStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PayablePayment payment) || id == null) {
            return false;
        }
        return id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
