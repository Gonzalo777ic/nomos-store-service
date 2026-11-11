package com.valeerp.treasury;

import com.valeerp.kernel.Actor;
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
 * Sesión de caja (apertura/cierre) con arqueo y diferencia documentada.
 * Libro de sesión: no delete ni soft-hide.
 */
@Entity
@Table(name = "cash_sessions")
public class CashSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cash_register_id", nullable = false)
    private Long cashRegisterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CashSessionStatus status;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "opened_by", nullable = false, length = 120)
    private String openedBy;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by", length = 120)
    private String closedBy;

    @Column(name = "opening_amount", nullable = false, precision = 19, scale = Money.SCALE)
    private BigDecimal openingAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "expected_amount", precision = 19, scale = Money.SCALE)
    private BigDecimal expectedAmount;

    @Column(name = "counted_amount", precision = 19, scale = Money.SCALE)
    private BigDecimal countedAmount;

    @Column(name = "difference_amount", precision = 19, scale = Money.SCALE)
    private BigDecimal differenceAmount;

    @Column(name = "difference_note", length = TreasuryPolicy.DIFFERENCE_NOTE_MAX_LENGTH)
    private String differenceNote;

    protected CashSession() {
    }

    public static CashSession open(Long cashRegisterId, Money openingCash, Actor actor, Instant when) {
        if (cashRegisterId == null) {
            throw new DomainException("La sesión exige una caja");
        }
        Money opening = requireNonNegative(openingCash);
        Actor resolved = actor == null ? Actor.SYSTEM : actor;
        CashSession session = new CashSession();
        session.cashRegisterId = cashRegisterId;
        session.status = CashSessionStatus.OPEN;
        session.openedAt = when == null ? Instant.now() : when;
        session.openedBy = resolved.id();
        session.openingAmount = opening.amount();
        session.currencyCode = opening.currency();
        return session;
    }

    /**
     * Cierra la sesión con arqueo. Si hay diferencia, exige nota documentada.
     */
    public void close(Money expectedCash, Money countedCash, String differenceNote, Actor actor, Instant when) {
        if (status != CashSessionStatus.OPEN) {
            throw new DomainException("Solo una sesión abierta se puede cerrar");
        }
        Money expected = requireSameCurrency(requireNonNegative(expectedCash));
        Money counted = requireSameCurrency(requireNonNegative(countedCash));
        Money difference = counted.minus(expected);
        if (difference.amount().signum() != 0
                && (differenceNote == null || differenceNote.isBlank())) {
            throw new DomainException("La diferencia de arqueo exige una nota documentada");
        }
        Actor resolved = actor == null ? Actor.SYSTEM : actor;
        this.expectedAmount = expected.amount();
        this.countedAmount = counted.amount();
        this.differenceAmount = difference.amount();
        this.differenceNote = blankToNull(differenceNote);
        this.status = CashSessionStatus.CLOSED;
        this.closedAt = when == null ? Instant.now() : when;
        this.closedBy = resolved.id();
    }

    public boolean isOpen() {
        return status == CashSessionStatus.OPEN;
    }

    public boolean hasDifference() {
        return differenceAmount != null && differenceAmount.signum() != 0;
    }

    public Long getId() {
        return id;
    }

    public Long getCashRegisterId() {
        return cashRegisterId;
    }

    public CashSessionStatus getStatus() {
        return status;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public String getOpenedBy() {
        return openedBy;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public Money openingCash() {
        return Money.of(openingAmount, currencyCode);
    }

    public Money expectedCash() {
        return expectedAmount == null ? null : Money.of(expectedAmount, currencyCode);
    }

    public Money countedCash() {
        return countedAmount == null ? null : Money.of(countedAmount, currencyCode);
    }

    public Money difference() {
        return differenceAmount == null ? null : Money.of(differenceAmount, currencyCode);
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getDifferenceNote() {
        return differenceNote;
    }

    private Money requireSameCurrency(Money money) {
        if (!currencyCode.equals(money.currency())) {
            throw new DomainException("La moneda del arqueo no coincide con la de la sesión");
        }
        return money;
    }

    private static Money requireNonNegative(Money money) {
        if (money == null) {
            throw new DomainException("El monto de caja es obligatorio");
        }
        if (money.isNegative()) {
            throw new DomainException("El monto de caja no puede ser negativo");
        }
        return money;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > TreasuryPolicy.DIFFERENCE_NOTE_MAX_LENGTH) {
            throw new DomainException("La nota de diferencia excede la longitud permitida");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CashSession session)) {
            return false;
        }
        return id != null && id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
