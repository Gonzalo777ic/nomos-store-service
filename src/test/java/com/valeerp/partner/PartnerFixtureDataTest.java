package com.valeerp.partner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartnerFixtureDataTest {

    @Test
    void completeRucProducesValidChecksum() {
        DocumentFormatPolicy policy = DocumentFormatPolicy.withDefaults();
        for (PartnerFixtureData.PartnerSeed seed : PartnerFixtureData.partners()) {
            if (seed.documentType() != DocumentType.RUC) {
                continue;
            }
            String ruc = PartnerFixtureData.completeRuc(seed.documentBase());
            assertEquals(DocumentRuleCatalog.RUC_LENGTH, ruc.length());
            assertTrue(RucChecksum.matches(ruc));
            IdentityDocument document = policy.require(DocumentType.RUC, ruc);
            assertEquals(ruc, document.number());
        }
    }

    @Test
    void seedIncludesPersonaEmpresaAndMixtoTags() {
        boolean persona = false;
        boolean empresa = false;
        boolean mixto = false;
        for (PartnerFixtureData.PartnerSeed seed : PartnerFixtureData.partners()) {
            if (seed.tags().contains("persona")) {
                persona = true;
            }
            if (seed.tags().contains("empresa")) {
                empresa = true;
            }
            if (seed.tags().stream().anyMatch(tag -> tag.contains("mixto"))) {
                mixto = true;
            }
        }
        assertTrue(persona);
        assertTrue(empresa);
        assertTrue(mixto);
    }
}
