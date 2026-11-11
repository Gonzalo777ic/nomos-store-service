package com.valeerp.accounting;

import com.valeerp.kernel.DomainException;

public enum PaymentDirection {
    /** Cobro (aplicar a facturas cliente). */
    INBOUND,
    /** Pago (aplicar a facturas proveedor). */
    OUTBOUND;

    public static PaymentDirection require(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("La dirección del pago es obligatoria");
        }
        try {
            return PaymentDirection.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Dirección de pago desconocida: " + raw);
        }
    }

    public InvoiceSide expectedInvoiceSide() {
        return this == INBOUND ? InvoiceSide.CUSTOMER : InvoiceSide.VENDOR;
    }
}
