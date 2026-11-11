package com.valeerp.kernel;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class Actors {

    private Actors() {
    }

    public static Actor currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return Actor.SYSTEM;
        }
        return Actor.of(authentication.getName());
    }

    /** Identificador textual del actor actual (compatibilidad con retiros existentes). */
    public static String current() {
        return currentActor().id();
    }
}
