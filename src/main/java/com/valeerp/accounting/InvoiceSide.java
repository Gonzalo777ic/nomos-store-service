package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;

public enum InvoiceSide {
    CUSTOMER,
    VENDOR;

    public static InvoiceSide require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El lado de la factura es obligatorio");
        }
        try {
            return InvoiceSide.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Lado de factura desconocido: " + raw);
        }
    }
}
