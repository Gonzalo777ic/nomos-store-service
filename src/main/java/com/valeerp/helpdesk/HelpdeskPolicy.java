package com.valeerp.helpdesk;

/**
 * Política local del contexto Helpdesk. SLA por defecto vive aquí, no en controladores.
 */
public final class HelpdeskPolicy {

    /** Horas máximas hasta primera respuesta cuando no hay SLA maestro. */
    public static final int DEFAULT_RESPONSE_HOURS = 8;

    /** Horas máximas hasta resolución cuando no hay SLA maestro. */
    public static final int DEFAULT_RESOLUTION_HOURS = 48;

    public static final int MIN_SLA_HOURS = 1;

    private HelpdeskPolicy() {
    }
}
