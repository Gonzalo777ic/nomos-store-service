package com.valeerp.treasury;

/**
 * Estados del documento de transferencia caja ↔ banco. Anulación por reverso, no delete.
 */
public enum TransferStatus {
    DRAFT,
    POSTED,
    VOIDED
}
