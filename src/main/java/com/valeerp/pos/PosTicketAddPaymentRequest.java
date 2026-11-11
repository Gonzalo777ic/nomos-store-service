package com.valeerp.pos;

import java.math.BigDecimal;

public record PosTicketAddPaymentRequest(
        Long paymentMethodId,
        BigDecimal amount
) {
}
