package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GeneralLedgerMovementResponse(
        Long entryId,
        LocalDate entryDate,
        String concept,
        String referenceDocument,
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal runningBalance
) {
}
