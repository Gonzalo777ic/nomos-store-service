package com.valeerp.sales;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Política de facturación del pedido de venta (estilo Odoo): por pedido, por entrega o manual.
 */
public enum InvoicingPolicy {
    ORDERED,
    DELIVERED,
    MANUAL;

    public static InvoicingPolicy require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("La política de facturación es obligatoria");
        }
        try {
            return InvoicingPolicy.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Política de facturación no soportada: " + raw);
        }
    }

    public static InvoicingPolicy orDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            return ORDERED;
        }
        return require(raw);
    }
}
