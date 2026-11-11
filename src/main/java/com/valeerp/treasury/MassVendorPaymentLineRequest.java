package com.valeerp.treasury;

import java.math.BigDecimal;

public record MassVendorPaymentLineRequest(
        Long partnerId,
        Long payableDocumentId,
        BigDecimal amount,
        String currency,
        String concept
) {
}
