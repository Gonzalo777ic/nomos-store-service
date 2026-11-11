package com.valeerp.project;

/**
 * Ciclo de vida del proyecto. Archivo definitivo vía soft-delete en el maestro.
 */
public enum ProjectStatus {
    DRAFT,
    OPEN,
    DONE,
    CANCELLED;

    public boolean allowsPlanning() {
        return this == DRAFT || this == OPEN;
    }

    public boolean allowsWork() {
        return this == OPEN;
    }

    public boolean isTerminal() {
        return this == DONE || this == CANCELLED;
    }
}
