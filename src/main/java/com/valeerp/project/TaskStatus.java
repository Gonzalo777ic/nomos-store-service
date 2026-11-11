package com.valeerp.project;

/**
 * Estado de tarea. Libro de trabajo: cierre/cancelación por estado, no delete.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    CANCELLED;

    public boolean isOpen() {
        return this == TODO || this == IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == DONE || this == CANCELLED;
    }
}
