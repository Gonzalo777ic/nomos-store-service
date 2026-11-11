package com.valeerp.pos;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Devolución POS. Libro con estados; confirmación aplica cantidades en el ticket y documento de reverso.
 */
@Entity
@Table(name = "pos_returns")
public class PosReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PosReturnStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "refund_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundTotal;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @OneToMany(mappedBy = "posReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PosReturnLine> lines = new ArrayList<>();

    protected PosReturn() {
    }

    public static PosReturn draft(PosTicket ticket, String reason, Instant createdAt) {
        if (ticket == null || ticket.getId() == null) {
            throw new DomainException("El ticket es obligatorio");
        }
        if (ticket.getStatus() != PosTicketStatus.POSTED && ticket.getStatus() != PosTicketStatus.PARTIALLY_RETURNED) {
            throw new DomainException("Solo se devuelven tickets publicados");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("El motivo de devolución es obligatorio");
        }
        PosReturn posReturn = new PosReturn();
        posReturn.ticketId = ticket.getId();
        posReturn.status = PosReturnStatus.DRAFT;
        posReturn.reason = reason.trim();
        posReturn.currency = ticket.getCurrency();
        posReturn.refundTotal = BigDecimal.ZERO.setScale(Money.SCALE, Money.ROUNDING);
        posReturn.createdAt = createdAt == null ? Instant.now() : createdAt;
        return posReturn;
    }

    public void addLine(PosTicketLine ticketLine, int quantity) {
        assertDraft();
        PosReturnLine line = PosReturnLine.of(this, ticketLine, quantity);
        lines.add(line);
        recalculate();
    }

    public void confirm(Instant when) {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("La devolución no tiene líneas");
        }
        this.status = PosReturnStatus.CONFIRMED;
        this.confirmedAt = when == null ? Instant.now() : when;
    }

    public void cancel() {
        assertDraft();
        this.status = PosReturnStatus.CANCELLED;
    }

    public Money refundMoney() {
        return Money.of(refundTotal, currency);
    }

    private void recalculate() {
        Money total = Money.zero(currency);
        for (PosReturnLine line : lines) {
            total = total.plus(line.refundMoney());
        }
        this.refundTotal = total.amount();
    }

    private void assertDraft() {
        if (status != PosReturnStatus.DRAFT) {
            throw new DomainException("La devolución ya no está en borrador");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public PosReturnStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public List<PosReturnLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PosReturn posReturn)) {
            return false;
        }
        return id != null && id.equals(posReturn.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
