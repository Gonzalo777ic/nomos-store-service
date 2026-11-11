package com.valeerp.accounting;

/**
 * DRAFT → POSTED → REVERSED (anulado con contra-asiento).
 * REVERSED sigue en el libro; nunca se borra un asiento publicado.
 */
public enum JournalEntryStatus {
    DRAFT,
    POSTED,
    REVERSED
}
