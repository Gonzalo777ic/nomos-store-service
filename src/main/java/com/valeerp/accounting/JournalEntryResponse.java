package com.valeerp.accounting;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record JournalEntryResponse(
        Long id,
        Long companyId,
        Long journalId,
        LocalDate entryDate,
        String concept,
        String referenceDocument,
        String status,
        String currencyCode,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        Long reversesEntryId,
        Long reversedByEntryId,
        Instant postedAt,
        Instant voidedAt,
        List<JournalEntryLineResponse> lines
) {

    public static JournalEntryResponse from(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getCompanyId(),
                entry.getJournalId(),
                entry.getEntryDate(),
                entry.getConcept(),
                entry.getReferenceDocument(),
                entry.getStatus().name(),
                entry.getCurrencyCode(),
                entry.totalDebit().amount(),
                entry.totalCredit().amount(),
                entry.getReversesEntryId(),
                entry.getReversedByEntryId(),
                entry.getPostedAt(),
                entry.getVoidedAt(),
                entry.getLines().stream().map(JournalEntryLineResponse::from).toList()
        );
    }
}
