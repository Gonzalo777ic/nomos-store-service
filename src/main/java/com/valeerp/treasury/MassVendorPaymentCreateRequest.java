package com.valeerp.treasury;

import java.util.List;

public record MassVendorPaymentCreateRequest(
        Long companyId,
        String reference,
        Long paymentMethodId,
        Long bankAccountId,
        Long cashRegisterId,
        Long cashSessionId,
        String currency,
        String concept,
        List<MassVendorPaymentLineRequest> lines
) {
}
