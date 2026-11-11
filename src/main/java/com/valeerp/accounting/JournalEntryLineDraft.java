package com.valeerp.accounting;

import java.math.BigDecimal;

public record JournalEntryLineDraft(
        String accountCode,
        String accountName,
        String accountClass,
        BigDecimal debit,
        BigDecimal credit,
        String memo
) {
}
