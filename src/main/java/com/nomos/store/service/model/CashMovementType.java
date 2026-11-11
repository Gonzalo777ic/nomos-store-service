package com.nomos.store.service.model;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Tipos del libro de movimientos de efectivo/banco.
 */
public enum CashMovementType {

    INCOME("Ingreso"),
    EXPENSE("Egreso"),
    TRANSFER_OUT("Salida por transferencia"),
    TRANSFER_IN("Entrada por transferencia"),
    ADJUSTMENT("Ajuste de arqueo"),
    COMPENSATION("Compensación / anulación");

    private final String label;

    CashMovementType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean affectsCashBalancePositively() {
        return this == INCOME || this == TRANSFER_IN || this == ADJUSTMENT;
    }

    public static CashMovementType require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de movimiento es obligatorio");
        }
        try {
            return CashMovementType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de movimiento no soportado: " + raw);
        }
    }
}
