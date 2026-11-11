package com.valeerp.crm;

/**
 * Ciclo de vida de una actividad. Libro: cierre por complete/cancel, sin delete físico.
 */
public enum ActivityStatus {
    PLANNED,
    COMPLETED,
    CANCELLED;

    public boolean isOpen() {
        return this == PLANNED;
    }
}
