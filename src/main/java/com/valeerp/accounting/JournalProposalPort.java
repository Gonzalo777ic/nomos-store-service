package com.valeerp.accounting;

/**
 * Puerto interno: documentos locales proponen asientos sin Feign a otros contextos.
 */
public interface JournalProposalPort {

    JournalEntry proposeAndPost(JournalEntryDraftRequest draft);
}
