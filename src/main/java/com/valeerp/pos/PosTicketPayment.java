package com.valeerp.pos;

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

@Entity
@Table(name = "pos_ticket_payments")
public class PosTicketPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private PosTicket ticket;

    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    protected PosTicketPayment() {
    }

    static PosTicketPayment of(PosTicket ticket, Long paymentMethodId, Money amount) {
        if (paymentMethodId == null) {
            throw new DomainException("paymentMethodId es obligatorio");
        }
        if (amount == null || amount.amount().signum() <= 0) {
            throw new DomainException("El monto de pago debe ser positivo");
        }
        PosTicketPayment payment = new PosTicketPayment();
        payment.ticket = ticket;
        payment.paymentMethodId = paymentMethodId;
        payment.amount = amount.amount();
        payment.currency = amount.currency();
        return payment;
    }

    Money money() {
        return Money.of(amount, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
