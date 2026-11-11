package com.valeerp.pos;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PosTicketAggregateTest {

    private static final String PEN = Currency.PEN.code();
    private static final BigDecimal IGV = new BigDecimal("0.18");

    @Test
    void closesTotalsFromUnitPriceAndPersistedTaxIgnoringClientSubtotals() {
        PosTicket ticket = openTicket();
        ticket.addLine(10L, "Café", 2, Money.of(new BigDecimal("11.80"), PEN), 1L, IGV);

        assertMoney(new BigDecimal("20.00"), ticket.baseMoney());
        assertMoney(new BigDecimal("3.60"), ticket.taxMoney());
        assertMoney(new BigDecimal("23.60"), ticket.totalMoney());
    }

    @Test
    void postsOnlyWhenMixedPaymentsEqualTotal() {
        PosTicket ticket = openTicket();
        ticket.addLine(1L, "Pan", 1, Money.of(new BigDecimal("10.00"), PEN), 1L, BigDecimal.ZERO);
        ticket.addPayment(100L, Money.of(new BigDecimal("4.00"), PEN));
        ticket.addPayment(101L, Money.of(new BigDecimal("6.00"), PEN));

        ticket.post(Instant.parse("2026-11-10T12:00:00Z"));

        assertEquals(PosTicketStatus.POSTED, ticket.getStatus());
    }

    @Test
    void rejectsPostWhenPaymentsDoNotCoverTotal() {
        PosTicket ticket = openTicket();
        ticket.addLine(1L, "Pan", 1, Money.of(new BigDecimal("10.00"), PEN), 1L, BigDecimal.ZERO);
        ticket.addPayment(100L, Money.of(new BigDecimal("9.00"), PEN));

        DomainException error = assertThrows(DomainException.class, () -> ticket.post(Instant.now()));
        assertTrue(error.getMessage().contains("pagos"));
    }

    @Test
    void voidCreatesTerminalStateWithoutDeletingTicket() {
        PosTicket ticket = postedTicket();
        ticket.markVoided("Error de cobro", Instant.parse("2026-11-10T13:00:00Z"));

        assertEquals(PosTicketStatus.VOIDED, ticket.getStatus());
        assertEquals("Error de cobro", ticket.getVoidReason());
        assertEquals(1, ticket.getLines().size());
    }

    @Test
    void returnTracksPartialAndFullStatuses() {
        PosTicket ticket = postedTicket();
        PosTicketLine line = ticket.getLines().get(0);
        // Simulate persisted id for requireLine in return flow
        setLineId(line, 50L);

        PosReturn posReturn = PosReturn.draft(ticket, "Cliente insatisfecho", Instant.now());
        posReturn.addLine(line, 1);
        line.registerReturn(1);
        posReturn.confirm(Instant.now());
        ticket.applyConfirmedReturn();

        assertEquals(PosTicketStatus.RETURNED, ticket.getStatus());
        assertEquals(PosReturnStatus.CONFIRMED, posReturn.getStatus());
        assertMoney(new BigDecimal("10.00"), posReturn.refundMoney());
    }

    private static PosTicket openTicket() {
        PosConfig config = PosConfig.create(1L, "CAJA-1", "Mostrador", 7L, 3L, 9L, PEN, Instant.now());
        setConfigId(config, 1L);
        return PosTicket.open(config, 20L, null, Instant.now());
    }

    private static PosTicket postedTicket() {
        PosTicket ticket = openTicket();
        ticket.addLine(1L, "Pan", 1, Money.of(new BigDecimal("10.00"), PEN), 1L, BigDecimal.ZERO);
        ticket.addPayment(100L, Money.of(new BigDecimal("10.00"), PEN));
        ticket.post(Instant.now());
        setTicketId(ticket, 77L);
        return ticket;
    }

    private static void assertMoney(BigDecimal expected, Money actual) {
        assertEquals(0, expected.setScale(Money.SCALE, Money.ROUNDING).compareTo(actual.amount()));
        assertEquals(PEN, actual.currency());
    }

    private static void setConfigId(PosConfig config, Long id) {
        try {
            var field = PosConfig.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(config, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setTicketId(PosTicket ticket, Long id) {
        try {
            var field = PosTicket.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ticket, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setLineId(PosTicketLine line, Long id) {
        try {
            var field = PosTicketLine.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(line, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
