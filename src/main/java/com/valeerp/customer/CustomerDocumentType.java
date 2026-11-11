package com.valeerp.customer;

import com.valeerp.partner.DocumentType;

/**
 * @deprecated Usar {@link DocumentType} / {@link com.valeerp.partner.PartnerDocumentPolicy}.
 * Conservado solo como alias de migración para referencias legacy.
 */
@Deprecated(forRemoval = false)
public final class CustomerDocumentType {

    private CustomerDocumentType() {
    }

    public static DocumentType[] values() {
        return DocumentType.values();
    }
}
