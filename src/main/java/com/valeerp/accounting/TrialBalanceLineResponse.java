package com.valeerp.accounting;

import java.math.BigDecimal;

public record TrialBalanceLineResponse(
        String accountCode,
        String accountName,
        String accountClass,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal balanceDebit,
        BigDecimal balanceCredit
) {
}
