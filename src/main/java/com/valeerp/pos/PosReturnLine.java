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
@Table(name = "pos_return_lines")
public class PosReturnLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_id", nullable = false)
    private PosReturn posReturn;

    @Column(name = "ticket_line_id", nullable = false)
    private Long ticketLineId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "refund_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    protected PosReturnLine() {
    }

    static PosReturnLine of(PosReturn posReturn, PosTicketLine ticketLine, int quantity) {
        if (ticketLine == null || ticketLine.getId() == null) {
            throw new DomainException("La línea de ticket es obligatoria");
        }
        Money refund = ticketLine.refundFor(quantity);
        PosReturnLine line = new PosReturnLine();
        line.posReturn = posReturn;
        line.ticketLineId = ticketLine.getId();
        line.quantity = quantity;
        line.refundAmount = refund.amount();
        line.currency = refund.currency();
        return line;
    }

    Money refundMoney() {
        return Money.of(refundAmount, currency);
    }

    public Long getId() {
        return id;
    }

    public Long getTicketLineId() {
        return ticketLineId;
    }

    public int getQuantity() {
        return quantity;
    }
}
