package com.valeerp.treasury;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Clases de método de pago persistibles (efectivo, tarjeta, transferencia, billetera).
 */
public enum PaymentMethodKind {

    CASH("Efectivo"),
    CARD("Tarjeta"),
    TRANSFER("Transferencia"),
    WALLET("Billetera");

    private final String label;

    PaymentMethodKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static PaymentMethodKind require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de método de pago es obligatorio");
        }
        try {
            return PaymentMethodKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de método de pago no soportado: " + raw);
        }
    }
}
