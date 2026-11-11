package com.valeerp.crm;

import com.valeerp.kernel.Currency;

/**
 * Política de dominio del contexto CRM. Moneda y reglas fijas viven aquí, no en controladores.
 */
public final class CrmPolicy {

    public static final Currency DEFAULT_CURRENCY = Currency.PEN;

    public static final int DEFAULT_STAGE_SEQUENCE_STEP = 10;

    private CrmPolicy() {
    }
}
