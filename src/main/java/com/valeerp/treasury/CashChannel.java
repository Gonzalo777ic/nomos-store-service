package com.valeerp.treasury;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Canal del movimiento de tesorería: caja física o banco.
 */
public enum CashChannel {

    CASH_REGISTER("Caja"),
    BANK("Banco");

    private final String label;

    CashChannel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static CashChannel require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El canal de tesorería es obligatorio");
        }
        try {
            return CashChannel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Canal de tesorería no soportado: " + raw);
        }
    }
}
