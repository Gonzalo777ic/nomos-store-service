package com.valeerp.kernel;

/**
 * Identidad del actor que ejecuta una operación de dominio (baja lógica, auditoría).
 * No es el User de identidad: solo el identificador operativo local.
 */
public final class Actor {

    public static final Actor SYSTEM = new Actor("sistema");

    private final String id;

    private Actor(String id) {
        this.id = id;
    }

    public static Actor of(String id) {
        if (id == null || id.isBlank()) {
            return SYSTEM;
        }
        return new Actor(id.trim());
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Actor actor)) {
            return false;
        }
        return id.equals(actor.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
