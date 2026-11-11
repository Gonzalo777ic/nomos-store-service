package com.valeerp.partner;

import com.valeerp.kernel.DomainException;

/**
 * Política pura de formato documental. Extensible vía {@link DocumentRuleCatalog}
 * (añadir tipo al enum + regla) sin tocar la capa HTTP.
 */
public final class DocumentFormatPolicy {

    private final DocumentRuleCatalog catalog;

    public DocumentFormatPolicy(DocumentRuleCatalog catalog) {
        this.catalog = catalog == null ? DocumentRuleCatalog.defaults() : catalog;
    }

    public static DocumentFormatPolicy withDefaults() {
        return new DocumentFormatPolicy(DocumentRuleCatalog.defaults());
    }

    public DocumentRuleCatalog catalog() {
        return catalog;
    }

    public IdentityDocument require(DocumentType type, String rawNumber) {
        if (type == null) {
            throw new DomainException("El tipo de documento es obligatorio");
        }
        if (rawNumber == null || rawNumber.isBlank()) {
            throw new DomainException("El número de documento es obligatorio");
        }
        String normalized = normalize(type, rawNumber);
        DocumentFormatRule rule = catalog.require(type);
        if (normalized.length() < rule.minLength() || normalized.length() > rule.maxLength()) {
            throw new DomainException(lengthMessage(type, rule));
        }
        if (!rule.pattern().matcher(normalized).matches()) {
            throw new DomainException("El " + type.name() + " no cumple el formato configurado");
        }
        if (rule.checksumRequired() && type == DocumentType.RUC && !RucChecksum.matches(normalized)) {
            throw new DomainException("El RUC tiene dígito verificador inválido");
        }
        return IdentityDocument.ofValidated(type, normalized);
    }

    public IdentityDocument require(String typeRaw, String rawNumber) {
        return require(DocumentType.require(typeRaw), rawNumber);
    }

    /**
     * Validación opcional: si ambos campos vienen vacíos, no exige documento.
     * Si uno viene informado, exige el par completo y aplica la política.
     */
    public IdentityDocument requireOptional(String typeRaw, String rawNumber) {
        boolean typeBlank = typeRaw == null || typeRaw.isBlank();
        boolean numberBlank = rawNumber == null || rawNumber.isBlank();
        if (typeBlank && numberBlank) {
            return null;
        }
        if (typeBlank) {
            throw new DomainException("El tipo de documento es obligatorio cuando se informa el número");
        }
        if (numberBlank) {
            throw new DomainException("El número de documento es obligatorio cuando se informa el tipo");
        }
        return require(typeRaw, rawNumber);
    }

    static String normalize(DocumentType type, String raw) {
        String trimmed = raw.trim().replace(" ", "");
        return switch (type) {
            case DNI, RUC -> trimmed;
            case PASSPORT, CE -> trimmed.toUpperCase();
        };
    }

    private static String lengthMessage(DocumentType type, DocumentFormatRule rule) {
        if (rule.minLength() == rule.maxLength()) {
            return "El " + type.name() + " debe tener " + rule.minLength() + " caracteres";
        }
        return "El " + type.name() + " debe tener entre " + rule.minLength()
                + " y " + rule.maxLength() + " caracteres";
    }
}
