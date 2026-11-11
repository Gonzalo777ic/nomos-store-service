package com.valeerp.helpdesk;

/**
 * Ciclo de vida del ticket. Cierre/cancelación por estado; no delete físico.
 */
public enum TicketStatus {
    NEW,
    IN_PROGRESS,
    WAITING,
    RESOLVED,
    CLOSED,
    CANCELLED;

    public boolean isOpen() {
        return this == NEW || this == IN_PROGRESS || this == WAITING;
    }

    public boolean isTerminal() {
        return this == CLOSED || this == CANCELLED;
    }

    public boolean countsAsResolved() {
        return this == RESOLVED || this == CLOSED;
    }
}
