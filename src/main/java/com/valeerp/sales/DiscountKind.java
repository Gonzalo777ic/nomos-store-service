package com.valeerp.sales;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Forma del descuento de línea o global. La magnitud vive en {@link Discount}.
 */
public enum DiscountKind {
    PERCENT,
    FIXED_AMOUNT;

    public static DiscountKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de descuento es obligatorio");
        }
        try {
            return DiscountKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de descuento no soportado: " + raw);
        }
    }

    public static DiscountKind optional(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return require(raw);
    }
}
