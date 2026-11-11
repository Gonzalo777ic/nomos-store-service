package com.valeerp.crm;

/**
 * Estado del documento oportunidad. Cierre por ganado/perdido/cancelado; no soft-delete.
 */
public enum OpportunityStatus {
    OPEN,
    WON,
    LOST,
    CANCELLED;

    public boolean isOpen() {
        return this == OPEN;
    }
}
