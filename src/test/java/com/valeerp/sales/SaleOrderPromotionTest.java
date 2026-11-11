package com.valeerp.sales;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleOrderPromotionTest {

    private static final String PEN = Currency.PEN.code();

    @Test
    void calculatesTotalsInsideAggregateIgnoringClientSubtotal() {
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        SaleOrder order = SaleOrder.draft(
                10L, null, PEN, now.plus(7, ChronoUnit.DAYS),
                InvoicingPolicy.ORDERED, null, now
        );

        order.addLine(1L, "Producto A", 2, Money.of(new BigDecimal("100.00"), PEN));

        assertEquals(new BigDecimal("200.00"), order.getSubtotalAmount());
        assertEquals(new BigDecimal("0.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("200.00"), order.getTotalAmount());
    }

    @Test
    void appliesPercentPromotionInsideAggregate() {
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        SaleOrder order = SaleOrder.draft(
                10L, null, PEN, now.plus(7, ChronoUnit.DAYS),
                InvoicingPolicy.DELIVERED, null, now
        );

        order.addLine(
                1L, "Producto A", 2, Money.of(new BigDecimal("100.00"), PEN),
                new PromotionSpec(5L, PromotionKind.PERCENT, new BigDecimal("10"), PEN)
        );

        assertEquals(new BigDecimal("200.00"), order.getSubtotalAmount());
        assertEquals(new BigDecimal("20.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("180.00"), order.getTotalAmount());
    }

    @Test
    void appliesTwoForOnePromotion() {
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        SaleOrder order = SaleOrder.draft(
                10L, null, PEN, now.plus(7, ChronoUnit.DAYS),
                InvoicingPolicy.MANUAL, null, now
        );

        order.addLine(
                1L, "Producto B", 3, Money.of(new BigDecimal("40.00"), PEN),
                new PromotionSpec(9L, PromotionKind.TWO_FOR_ONE, BigDecimal.ZERO, PEN)
        );

        assertEquals(new BigDecimal("120.00"), order.getSubtotalAmount());
        assertEquals(new BigDecimal("40.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("80.00"), order.getTotalAmount());
    }

    @Test
    void confirmIssuesCommercialDocumentSeparateFromFiscalInvoice() {
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        SaleOrder order = SaleOrder.draft(
                10L, null, PEN, now.plus(7, ChronoUnit.DAYS),
                InvoicingPolicy.ORDERED, null, now
        );
        order.addLine(1L, "Producto C", 1, Money.of(new BigDecimal("50.00"), PEN));

        SaleOrderDocument document = order.confirm("00000001", now);

        assertEquals(SaleOrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getCommercialDocument());
        assertEquals(SalesPolicy.SALE_ORDER_DOCUMENT_SERIES, document.getSeries());
        assertEquals("SO-00000001", document.fullNumber());
        assertEquals(InvoicingPolicy.ORDERED.name(), document.getInvoicingPolicy());
    }

    @Test
    void rejectsConfirmWhenQuotationExpired() {
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        SaleOrder order = SaleOrder.draft(
                10L, null, PEN, now.plus(1, ChronoUnit.DAYS),
                InvoicingPolicy.ORDERED, null, now
        );
        order.addLine(1L, "Producto D", 1, Money.of(new BigDecimal("10.00"), PEN));

        assertThrows(com.valeerp.kernel.DomainException.class,
                () -> order.confirm("00000002", now.plus(3, ChronoUnit.DAYS)));
    }
}
