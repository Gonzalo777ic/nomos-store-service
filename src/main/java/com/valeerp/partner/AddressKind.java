package com.valeerp.partner;

import com.valeerp.kernel.DomainException;

/**
 * Clasificación de dirección comercial del Partner.
 */
public enum AddressKind {
    BILLING("Facturación"),
    SHIPPING("Entrega"),
    OFFICE("Oficina"),
    OTHER("Otra");

    private final String label;

    AddressKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static AddressKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            return OTHER;
        }
        try {
            return AddressKind.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de dirección no soportado: " + raw);
        }
    }
}
