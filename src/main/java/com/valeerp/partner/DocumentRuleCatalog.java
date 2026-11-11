package com.valeerp.partner;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Catálogo de reglas documentales del contexto Contactos.
 * Los literales de longitud/patrón viven aquí (política de dominio), no en HTTP.
 */
public final class DocumentRuleCatalog {

    /** DNI peruano: 8 dígitos. */
    public static final int DNI_LENGTH = 8;
    public static final String DNI_PATTERN = "\\d{8}";

    /** RUC peruano: 11 dígitos + dígito verificador. */
    public static final int RUC_LENGTH = 11;
    public static final String RUC_PATTERN = "\\d{11}";

    public static final int PASSPORT_MIN_LENGTH = 6;
    public static final int PASSPORT_MAX_LENGTH = 12;
    public static final String PASSPORT_PATTERN = "[A-Za-z0-9]{6,12}";

    public static final int CE_MIN_LENGTH = 8;
    public static final int CE_MAX_LENGTH = 12;
    public static final String CE_PATTERN = "[A-Za-z0-9]{8,12}";

    private final Map<DocumentType, DocumentFormatRule> rules;

    private DocumentRuleCatalog(Map<DocumentType, DocumentFormatRule> rules) {
        this.rules = Collections.unmodifiableMap(new EnumMap<>(rules));
    }

    public static DocumentRuleCatalog defaults() {
        Map<DocumentType, DocumentFormatRule> map = new EnumMap<>(DocumentType.class);
        map.put(DocumentType.DNI, DocumentFormatRule.exact(
                DocumentType.DNI, DNI_LENGTH, DNI_PATTERN, false));
        map.put(DocumentType.RUC, DocumentFormatRule.exact(
                DocumentType.RUC, RUC_LENGTH, RUC_PATTERN, true));
        map.put(DocumentType.PASSPORT, DocumentFormatRule.range(
                DocumentType.PASSPORT, PASSPORT_MIN_LENGTH, PASSPORT_MAX_LENGTH, PASSPORT_PATTERN));
        map.put(DocumentType.CE, DocumentFormatRule.range(
                DocumentType.CE, CE_MIN_LENGTH, CE_MAX_LENGTH, CE_PATTERN));
        return new DocumentRuleCatalog(map);
    }

    public DocumentFormatRule require(DocumentType type) {
        DocumentFormatRule rule = rules.get(type);
        if (rule == null) {
            throw new com.valeerp.kernel.DomainException(
                    "No hay política de formato para el tipo de documento: " + type);
        }
        return rule;
    }

    public Map<DocumentType, DocumentFormatRule> all() {
        return rules;
    }

    public DocumentRuleCatalog overlay(DocumentType type, DocumentFormatRule rule) {
        Map<DocumentType, DocumentFormatRule> copy = new EnumMap<>(rules);
        copy.put(type, rule);
        return new DocumentRuleCatalog(copy);
    }
}
