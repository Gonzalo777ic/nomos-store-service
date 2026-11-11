package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Asignación pago↔factura. Libro hijo; no se borra físicamente desde API pública.
 */
@Entity
@Table(name = "accounting_payment_allocations")
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceDocument invoice;

    @Column(name = "amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal amountValue;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    protected PaymentAllocation() {
    }

    static PaymentAllocation link(Payment payment, InvoiceDocument invoice, Money amount) {
        if (payment == null || invoice == null) {
            throw new DomainException("La asignación exige pago y factura");
        }
        if (amount == null || !amount.isPositive()) {
            throw new DomainException("El monto asignado debe ser positivo");
        }
        PaymentAllocation allocation = new PaymentAllocation();
        allocation.payment = payment;
        allocation.invoice = invoice;
        allocation.amountValue = amount.amount();
        allocation.currencyCode = amount.currency();
        return allocation;
    }

    public Money amount() {
        return Money.of(amountValue, currencyCode);
    }

    public Long getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public InvoiceDocument getInvoice() {
        return invoice;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PaymentAllocation allocation) || id == null) {
            return false;
        }
        return id.equals(allocation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
