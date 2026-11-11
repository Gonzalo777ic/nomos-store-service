package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BalanceSheetResponse(
        Long companyId,
        LocalDate asOf,
        String currencyCode,
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal totalEquity,
        BigDecimal netIncome,
        boolean balanced,
        List<BalanceSheetSectionResponse> sections
) {
}
