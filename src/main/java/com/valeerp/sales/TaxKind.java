package com.valeerp.sales;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Tipos fiscales configurables del catálogo local (IGV, exonerado, etc.).
 */
public enum TaxKind {
    IGV("IGV"),
    EXEMPT("Exonerado"),
    UNAFFECTED("Inafecto"),
    OTHER("Otro");

    private final String label;

    TaxKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static TaxKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de impuesto es obligatorio");
        }
        try {
            return TaxKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de impuesto desconocido: " + raw);
        }
    }
}
