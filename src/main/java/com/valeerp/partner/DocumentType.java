package com.valeerp.partner;

/**
 * Tipos de documento de la ficha comercial Partner.
 * Añadir un valor aquí + regla en {@link DocumentRuleCatalog} extiende la política
 * sin tocar controladores HTTP. Validación: {@link DocumentFormatPolicy}.
 */
public enum DocumentType {
    DNI("Documento Nacional de Identidad"),
    RUC("Registro Único de Contribuyentes"),
    PASSPORT("Pasaporte"),
    CE("Carné de Extranjería");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static DocumentType require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new com.valeerp.kernel.DomainException("El tipo de documento es obligatorio");
        }
        try {
            return DocumentType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new com.valeerp.kernel.DomainException("Tipo de documento no soportado: " + raw);
        }
    }
}
