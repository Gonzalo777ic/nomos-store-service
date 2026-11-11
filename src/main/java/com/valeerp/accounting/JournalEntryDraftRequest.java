package com.valeerp.accounting;

import java.time.LocalDate;
import java.util.List;

public record JournalEntryDraftRequest(
        Long companyId,
        Long journalId,
        LocalDate entryDate,
        String concept,
        String referenceDocument,
        String currencyCode,
        List<JournalEntryLineDraft> lines
) {
}
