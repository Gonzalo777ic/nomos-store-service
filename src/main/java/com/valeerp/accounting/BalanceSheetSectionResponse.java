package com.valeerp.accounting;

import java.math.BigDecimal;
import java.util.List;

public record BalanceSheetSectionResponse(
        String section,
        BigDecimal total,
        List<BalanceSheetAccountResponse> accounts
) {

    public record BalanceSheetAccountResponse(
            String accountCode,
            String accountName,
            BigDecimal balance
    ) {
    }
}
