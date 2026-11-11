package com.valeerp.accounting;

import org.springframework.stereotype.Component;

/**
 * Stub interno de propuesta de asiento. Sin Feign; reutiliza el caso de uso local.
 */
@Component
public class InternalJournalProposalStub implements JournalProposalPort {

    private final JournalEntryService journalEntryService;

    public InternalJournalProposalStub(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @Override
    public JournalEntry proposeAndPost(JournalEntryDraftRequest draft) {
        return journalEntryService.proposeAndPost(draft);
    }
}
