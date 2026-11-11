package com.valeerp.crm;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Tipos de actividad CRM. Política de dominio; el UI consume el catálogo vía API.
 */
public enum ActivityType {
    CALL,
    EMAIL,
    MEETING;

    public static ActivityType require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de actividad es obligatorio");
        }
        try {
            return ActivityType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de actividad no soportado: " + raw);
        }
    }
}
