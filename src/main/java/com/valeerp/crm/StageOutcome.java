package com.valeerp.crm;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Resultado terminal que una etapa puede imponer sobre la oportunidad al moverse a ella.
 */
public enum StageOutcome {
    OPEN,
    WON,
    LOST;

    public boolean isTerminal() {
        return this != OPEN;
    }

    public static StageOutcome require(String raw) {
        if (raw == null || raw.isBlank()) {
            return OPEN;
        }
        try {
            return StageOutcome.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Resultado de etapa no soportado: " + raw);
        }
    }
}
