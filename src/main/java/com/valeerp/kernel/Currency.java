package com.valeerp.kernel;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Moneda ISO-4217 del contexto. La política local arranca con {@link #PEN}.
 */
public final class Currency implements Serializable {

    public static final Currency PEN = new Currency("PEN");

    private final String code;

    private Currency(String code) {
        this.code = code;
    }

    public static Currency of(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("La moneda es obligatoria");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (PEN.code.equals(normalized)) {
            return PEN;
        }
        if (normalized.length() != 3) {
            throw new DomainException("El código de moneda debe ser ISO-4217 (3 letras)");
        }
        return new Currency(normalized);
    }

    public String code() {
        return code;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Currency currency)) {
            return false;
        }
        return code.equals(currency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
