package com.valeerp.partner;

import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentFormatPolicyTest {

    private DocumentFormatPolicy policy;

    @BeforeEach
    void setUp() {
        policy = DocumentFormatPolicy.withDefaults();
    }

    @Test
    void acceptsValidDni() {
        IdentityDocument document = policy.require("DNI", "12345678");
        assertEquals(DocumentType.DNI, document.type());
        assertEquals("12345678", document.number());
    }

    @Test
    void rejectsShortDni() {
        DomainException ex = assertThrows(DomainException.class, () -> policy.require("DNI", "1234567"));
        assertTrue(ex.getMessage().contains("8"));
    }

    @Test
    void acceptsValidRucWithChecksum() {
        IdentityDocument document = policy.require(DocumentType.RUC, "20100070970");
        assertEquals("20100070970", document.number());
    }

    @Test
    void rejectsRucWithBadChecksum() {
        DomainException ex = assertThrows(DomainException.class,
                () -> policy.require(DocumentType.RUC, "20100070971"));
        assertTrue(ex.getMessage().toLowerCase().contains("verificador"));
    }

    @Test
    void normalizesPassportToUpperCase() {
        IdentityDocument document = policy.require("PASSPORT", "ab123456");
        assertEquals("AB123456", document.number());
    }

    @Test
    void optionalSkipsWhenBothBlank() {
        assertNull(policy.requireOptional(null, null));
        assertNull(policy.requireOptional("  ", ""));
    }

    @Test
    void optionalRequiresPairWhenPartial() {
        assertThrows(DomainException.class, () -> policy.requireOptional("DNI", null));
        assertThrows(DomainException.class, () -> policy.requireOptional(null, "12345678"));
    }

    @Test
    void propertiesCanDisableRucChecksum() {
        PartnerDocumentPolicyProperties properties = new PartnerDocumentPolicyProperties();
        properties.setRucChecksumEnabled(false);
        DocumentFormatPolicy relaxed = new DocumentFormatPolicy(
                PartnerDocumentPolicy.buildCatalog(properties));
        IdentityDocument document = relaxed.require(DocumentType.RUC, "20100070971");
        assertEquals("20100070971", document.number());
    }
}
