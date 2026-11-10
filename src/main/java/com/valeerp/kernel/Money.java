package com.valeerp.kernel;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(SCALE, ROUNDING);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new DomainException("El monto es obligatorio");
        }
        if (currency == null || currency.isBlank()) {
            throw new DomainException("La moneda es obligatoria");
        }
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new DomainException("La cantidad no puede ser negativa");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money times(BigDecimal factor) {
        if (factor == null) {
            throw new DomainException("El factor es obligatorio");
        }
        return new Money(amount.multiply(factor), currency);
    }

    public Money includedTax(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) {
            throw new DomainException("La tasa de impuesto no es válida");
        }
        BigDecimal divisor = BigDecimal.ONE.add(rate);
        BigDecimal base = amount.divide(divisor, SCALE, ROUNDING);
        return new Money(amount.subtract(base), currency);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    private void requireSameCurrency(Money other) {
        if (other == null || !currency.equals(other.currency)) {
            throw new DomainException("No se pueden operar montos de distinta moneda");
        }
    }
}
