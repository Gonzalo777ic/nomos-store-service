package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GeneralLedgerResponse(
        Long companyId,
        String accountCode,
        String accountName,
        String accountClass,
        LocalDate fromDate,
        LocalDate toDate,
        String currencyCode,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        List<GeneralLedgerMovementResponse> movements
) {
}
