package com.valeerp.pos;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Documento de reverso POS (anulación o devolución). Libro: no se borra ni se oculta.
 */
@Entity
@Table(name = "pos_reversal_documents")
public class PosReversalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "return_id")
    private Long returnId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PosReversalKind kind;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PosReversalDocument() {
    }

    public static PosReversalDocument voidOf(PosTicket ticket, String reason, Instant when) {
        return create(ticket.getId(), null, PosReversalKind.VOID, ticket.totalMoney(), reason, when);
    }

    public static PosReversalDocument returnOf(PosTicket ticket, Long returnId, Money amount, String reason, Instant when) {
        return create(ticket.getId(), returnId, PosReversalKind.RETURN, amount, reason, when);
    }

    private static PosReversalDocument create(Long ticketId, Long returnId, PosReversalKind kind,
                                              Money amount, String reason, Instant when) {
        if (ticketId == null) {
            throw new DomainException("ticketId es obligatorio en el reverso");
        }
        if (amount == null || amount.amount().signum() <= 0) {
            throw new DomainException("El monto del reverso debe ser positivo");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("El motivo del reverso es obligatorio");
        }
        PosReversalDocument document = new PosReversalDocument();
        document.ticketId = ticketId;
        document.returnId = returnId;
        document.kind = kind;
        document.amount = amount.amount();
        document.currency = amount.currency();
        document.reason = reason.trim();
        document.createdAt = when == null ? Instant.now() : when;
        return document;
    }

    public Long getId() {
        return id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getReturnId() {
        return returnId;
    }

    public PosReversalKind getKind() {
        return kind;
    }

    public Money money() {
        return Money.of(amount, currency);
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
