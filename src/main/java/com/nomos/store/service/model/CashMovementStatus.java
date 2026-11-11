package com.nomos.store.service.model;

/**
 * Estado del movimiento en el libro. Anulado permanece visible; no se oculta.
 */
public enum CashMovementStatus {
    PENDING,
    PROCESSED,
    ANNULLED
}
