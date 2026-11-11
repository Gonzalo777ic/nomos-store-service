package com.valeerp.crm;

/**
 * Ciclo de vida del lead. La conversión a oportunidad es irreversible en el agregado.
 */
public enum LeadStatus {
    OPEN,
    QUALIFIED,
    DISQUALIFIED,
    CONVERTED
}
