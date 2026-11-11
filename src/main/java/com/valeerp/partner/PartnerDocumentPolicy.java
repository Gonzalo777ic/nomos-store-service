package com.valeerp.partner;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adaptador de aplicación: combina catálogo de dominio + overrides de configuración
 * y expone validación a los casos de uso Partner / Customer.
 */
@Component
public class PartnerDocumentPolicy {

    private final DocumentFormatPolicy formatPolicy;

    public PartnerDocumentPolicy(PartnerDocumentPolicyProperties properties) {
        this.formatPolicy = new DocumentFormatPolicy(buildCatalog(properties));
    }

    public IdentityDocument require(String typeRaw, String rawNumber) {
        return formatPolicy.require(typeRaw, rawNumber);
    }

    public IdentityDocument require(DocumentType type, String rawNumber) {
        return formatPolicy.require(type, rawNumber);
    }

    public IdentityDocument requireOptional(String typeRaw, String rawNumber) {
        return formatPolicy.requireOptional(typeRaw, rawNumber);
    }

    public List<DocumentTypeView> catalog() {
        return Arrays.stream(DocumentType.values())
                .map(type -> new DocumentTypeView(type.name(), type.description()))
                .toList();
    }

    public DocumentFormatPolicy formatPolicy() {
        return formatPolicy;
    }

    static DocumentRuleCatalog buildCatalog(PartnerDocumentPolicyProperties properties) {
        DocumentRuleCatalog catalog = DocumentRuleCatalog.defaults();
        if (properties == null) {
            return catalog;
        }
        if (properties.getRucChecksumEnabled() != null) {
            DocumentFormatRule ruc = catalog.require(DocumentType.RUC)
                    .withChecksum(properties.getRucChecksumEnabled());
            catalog = catalog.overlay(DocumentType.RUC, ruc);
        }
        for (Map.Entry<String, PartnerDocumentPolicyProperties.RuleOverride> entry
                : properties.getRules().entrySet()) {
            DocumentType type = DocumentType.valueOf(entry.getKey().trim().toUpperCase(Locale.ROOT));
            PartnerDocumentPolicyProperties.RuleOverride override = entry.getValue();
            DocumentFormatRule base = catalog.require(type);
            Integer min = override.getExactLength() != null
                    ? override.getExactLength()
                    : override.getMinLength();
            Integer max = override.getExactLength() != null
                    ? override.getExactLength()
                    : override.getMaxLength();
            DocumentFormatRule next = base.withLengths(min, max).withPattern(override.getPattern());
            if (override.getChecksumEnabled() != null) {
                next = next.withChecksum(override.getChecksumEnabled());
            }
            catalog = catalog.overlay(type, next);
        }
        return catalog;
    }

    public record DocumentTypeView(String key, String description) {
    }
}
