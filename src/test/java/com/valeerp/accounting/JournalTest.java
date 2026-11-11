package com.valeerp.accounting;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JournalTest {

    private static final Long COMPANY_ID = 3L;
    private static final Instant NOW = Instant.parse("2026-11-10T15:00:00Z");

    @Test
    void opensSalesJournalForCompany() {
        Journal journal = Journal.open(
                COMPANY_ID, "VEN", "Diario de ventas", JournalKind.SALES, 101L, NOW);

        assertEquals(COMPANY_ID, journal.getCompanyId());
        assertEquals("VEN", journal.getCode());
        assertEquals(JournalKind.SALES, journal.getKind());
        assertEquals(101L, journal.getDefaultAccountId());
    }

    @Test
    void supportsAllConfiguredJournalKinds() {
        assertEquals(5, JournalKind.values().length);
        assertEquals("Ventas", JournalKind.SALES.label());
        assertEquals("Compras", JournalKind.PURCHASES.label());
        assertEquals("Banco", JournalKind.BANK.label());
        assertEquals("Caja", JournalKind.CASH.label());
        assertEquals("Misceláneo", JournalKind.MISC.label());
        assertEquals(JournalKind.MISC, JournalKind.require("MISCELLANEOUS"));
    }

    @Test
    void rejectsMissingCompany() {
        DomainException error = assertThrows(DomainException.class, () ->
                Journal.open(null, "CAJ", "Caja", JournalKind.CASH, null, NOW));

        assertTrue(error.getMessage().toLowerCase().contains("compañía"));
    }

    @Test
    void archiveRetiresJournalWithoutPhysicalDelete() {
        Journal journal = Journal.open(
                COMPANY_ID, "BAN", "Banco BCP", JournalKind.BANK, 104L, NOW);

        journal.archive(Actor.of("accountant"), NOW);

        assertTrue(journal.isRetired());
    }
}
