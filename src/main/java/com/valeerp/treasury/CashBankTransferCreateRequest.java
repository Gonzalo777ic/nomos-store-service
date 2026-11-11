package com.valeerp.treasury;

import java.math.BigDecimal;

public record CashBankTransferCreateRequest(
        Long companyId,
        String direction,
        Long cashRegisterId,
        Long cashSessionId,
        Long bankAccountId,
        BigDecimal amount,
        String currency,
        String concept
) {
}
