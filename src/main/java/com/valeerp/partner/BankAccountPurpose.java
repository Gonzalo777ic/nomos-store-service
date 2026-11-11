package com.valeerp.partner;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Uso de la cuenta bancaria del partner en cobranza / pago.
 */
public enum BankAccountPurpose {
    COLLECTION("Cobranza"),
    PAYMENT("Pago"),
    BOTH("Cobranza y pago");

    private final String label;

    BankAccountPurpose(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static BankAccountPurpose require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El propósito de la cuenta es obligatorio");
        }
        try {
            return BankAccountPurpose.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Propósito de cuenta no soportado: " + raw);
        }
    }
}
