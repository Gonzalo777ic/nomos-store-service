package com.valeerp.sales;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Tipos de promoción aplicables solo dentro del agregado de venta/pedido.
 */
public enum PromotionKind {
    PERCENT,
    AMOUNT,
    TWO_FOR_ONE;

    public static PromotionKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de promoción es obligatorio");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PERCENT", "PORCENTAJE", "PERCENTAGE" -> PERCENT;
            case "AMOUNT", "MONTO", "MONTO_FIJO", "FIXED" -> AMOUNT;
            case "TWO_FOR_ONE", "2X1", "DOS_POR_UNO", "BUY_ONE_GET_ONE" -> TWO_FOR_ONE;
            default -> {
                try {
                    yield PromotionKind.valueOf(normalized);
                } catch (IllegalArgumentException ex) {
                    throw new DomainException("Tipo de promoción no soportado: " + raw);
                }
            }
        };
    }
}
