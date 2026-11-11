package com.valeerp.accounting;

import java.math.BigDecimal;

public record JournalEntryLineResponse(
        Long id,
        String accountCode,
        String accountName,
        String accountClass,
        BigDecimal debit,
        BigDecimal credit,
        String currencyCode,
        String memo
) {

    public static JournalEntryLineResponse from(JournalEntryLine line) {
        return new JournalEntryLineResponse(
                line.getId(),
                line.getAccountCode(),
                line.getAccountName(),
                line.getAccountClass().name(),
                line.debit().amount(),
                line.credit().amount(),
                line.getCurrencyCode(),
                line.getMemo()
        );
    }
}
