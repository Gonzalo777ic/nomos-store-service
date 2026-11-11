package com.valeerp.accounting;

import com.valeerp.kernel.Actor;
import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChartAccountTest {

    private static final Long COMPANY_ID = 7L;
    private static final Instant NOW = Instant.parse("2026-11-10T12:00:00Z");

    @Test
    void buildsHierarchyUnderNonPostableParent() {
        ChartAccount group = ChartAccount.open(
                COMPANY_ID, "10", "Caja y bancos", AccountClass.ASSET, false, null, NOW);
        ChartAccount cash = ChartAccount.open(
                COMPANY_ID, "101", "Caja", AccountClass.ASSET, true, group, NOW);

        assertEquals("10", group.getCode());
        assertEquals("101", cash.getCode());
        assertEquals(group, cash.getParent());
        assertEquals(1, group.activeChildren().size());
        assertTrue(cash.isAllowsPosting());
        assertFalse(group.isAllowsPosting());
    }

    @Test
    void rejectsPostableParent() {
        ChartAccount leaf = ChartAccount.open(
                COMPANY_ID, "101", "Caja", AccountClass.ASSET, true, null, NOW);

        DomainException error = assertThrows(DomainException.class, () ->
                ChartAccount.open(COMPANY_ID, "1011", "Caja chica", AccountClass.ASSET, true, leaf, NOW));

        assertTrue(error.getMessage().contains("imputable"));
    }

    @Test
    void rejectsCrossCompanyParent() {
        ChartAccount parent = ChartAccount.open(
                COMPANY_ID, "10", "Caja y bancos", AccountClass.ASSET, false, null, NOW);

        DomainException error = assertThrows(DomainException.class, () ->
                ChartAccount.open(99L, "101", "Caja", AccountClass.ASSET, true, parent, NOW));

        assertTrue(error.getMessage().toLowerCase().contains("compañía"));
    }

    @Test
    void archiveMarksSubtreeRetired() {
        ChartAccount group = ChartAccount.open(
                COMPANY_ID, "10", "Caja y bancos", AccountClass.ASSET, false, null, NOW);
        ChartAccount cash = ChartAccount.open(
                COMPANY_ID, "101", "Caja", AccountClass.ASSET, true, group, NOW);

        group.archive(Actor.of("accountant"), NOW);

        assertTrue(group.isRetired());
        assertTrue(cash.isRetired());
        assertEquals(0, group.activeChildren().size());
    }
}
