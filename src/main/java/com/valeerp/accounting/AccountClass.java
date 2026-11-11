package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Clasificación contable estilo PCGE (activo, pasivo, patrimonio, ingreso, gasto, orden).
 * Usada por plan de cuentas y líneas de asiento.
 */
public enum AccountClass {
    ASSET("Activo"),
    LIABILITY("Pasivo"),
    EQUITY("Patrimonio"),
    INCOME("Ingreso"),
    EXPENSE("Gasto"),
    OFF_BALANCE("Cuentas de orden");

    private final String label;

    AccountClass(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static AccountClass require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("La clase de cuenta es obligatoria");
        }
        try {
            return AccountClass.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Clase de cuenta desconocida: " + raw);
        }
    }

    public boolean isBalanceSheet() {
        return this == ASSET || this == LIABILITY || this == EQUITY;
    }
}
