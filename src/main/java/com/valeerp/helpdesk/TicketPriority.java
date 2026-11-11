package com.valeerp.helpdesk;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

public enum TicketPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    public static TicketPriority require(String raw) {
        if (raw == null || raw.isBlank()) {
            return NORMAL;
        }
        try {
            return TicketPriority.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Prioridad de ticket no soportada: " + raw);
        }
    }
}
