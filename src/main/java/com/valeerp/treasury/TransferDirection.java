package com.valeerp.treasury;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Sentido de la extracción/depósito entre caja y banco.
 */
public enum TransferDirection {

    CASH_TO_BANK("Extracción a banco"),
    BANK_TO_CASH("Depósito desde banco");

    private final String label;

    TransferDirection(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static TransferDirection require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El sentido de la transferencia es obligatorio");
        }
        try {
            return TransferDirection.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Sentido de transferencia no soportado: " + raw);
        }
    }
}
