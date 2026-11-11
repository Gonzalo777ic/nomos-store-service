package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Tipos de diario configurables por compañía (#70). Valores de política, no códigos de cuenta.
 */
public enum JournalKind {
    SALES("Ventas"),
    PURCHASES("Compras"),
    BANK("Banco"),
    CASH("Caja"),
    MISC("Misceláneo");

    private final String label;

    JournalKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static JournalKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de diario es obligatorio");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("MISCELLANEOUS".equals(normalized)) {
            return MISC;
        }
        try {
            return JournalKind.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de diario desconocido: " + raw);
        }
    }
}
