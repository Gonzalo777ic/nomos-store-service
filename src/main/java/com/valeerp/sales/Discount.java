package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;

/**
 * Descuento de dominio (porcentaje o monto fijo). Nunca recalcula el cliente.
 */
public final class Discount {

    private final DiscountKind kind;
    private final BigDecimal value;

    private Discount(DiscountKind kind, BigDecimal value) {
        this.kind = kind;
        this.value = value;
    }

    public static Discount none() {
        return new Discount(null, BigDecimal.ZERO);
    }

    public static Discount of(DiscountKind kind, BigDecimal value) {
        if (kind == null) {
            return none();
        }
        if (value == null) {
            throw new DomainException("El valor del descuento es obligatorio");
        }
        if (value.signum() < 0) {
            throw new DomainException("El descuento no puede ser negativo");
        }
        if (kind == DiscountKind.PERCENT && value.compareTo(new BigDecimal("100")) > 0) {
            throw new DomainException("El descuento porcentual no puede superar 100");
        }
        return new Discount(kind, value);
    }

    public static Discount optional(String kindRaw, BigDecimal value) {
        DiscountKind kind = DiscountKind.optional(kindRaw);
        if (kind == null) {
            return none();
        }
        return of(kind, value == null ? BigDecimal.ZERO : value);
    }

    public boolean isNone() {
        return kind == null || value.signum() == 0;
    }

    public Money applyTo(Money base) {
        if (isNone() || base == null || base.isZero()) {
            return Money.zero(base == null ? SalesPolicy.DEFAULT_CURRENCY : base.currency());
        }
        Money raw = switch (kind) {
            case PERCENT -> base.times(percentRatio());
            case FIXED_AMOUNT -> Money.of(value, base.currency());
        };
        return raw.cappedAt(base);
    }

    public DiscountKind kind() {
        return kind;
    }

    public BigDecimal value() {
        return value;
    }

    private BigDecimal percentRatio() {
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return value.movePointLeft(2);
        }
        return value;
    }
}
