package com.valeerp.project;

/**
 * Parte de horas. Anulación por estado CANCELLED; no delete físico.
 */
public enum TimesheetStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED;

    public boolean countsTowardTotals() {
        return this == CONFIRMED;
    }

    public boolean isMutable() {
        return this == DRAFT;
    }
}
