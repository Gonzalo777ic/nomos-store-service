package com.nomos.store.service.model;

public enum AccountsReceivableStatus {
    ACTIVE,     // Deuda vigente
    PAID,       // Totalmente pagada
    CANCELLED,  // Anulada administrativamente (Error de emisión, devolución total)
    BAD_DEBT    // Incobrable (Castigo de deuda)
}