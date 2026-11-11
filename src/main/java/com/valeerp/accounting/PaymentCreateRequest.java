package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentCreateRequest(
        Long companyId,
        Long partnerId,
        String direction,
        LocalDate paymentDate,
        BigDecimal amount,
        String currencyCode,
        String referenceNumber
) {
}
