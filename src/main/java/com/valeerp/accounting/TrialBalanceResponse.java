package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TrialBalanceResponse(
        Long companyId,
        LocalDate fromDate,
        LocalDate toDate,
        String currencyCode,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        List<TrialBalanceLineResponse> lines
) {
}
