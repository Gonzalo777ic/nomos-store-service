package com.valeerp.sales;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * #68 — totales de venta multi-línea (promos + impuesto + descuento global) con Money.
 */
class SaleTotalsTest {

    private static final String PEN = Currency.PEN.code();
    private static final BigDecimal FIXTURE_IGV_RATE = new BigDecimal("0.18");

    @Test
    void aggregatesMultiplePricedLinesIntoSaleTotals() {
        PricedLine first = PricedLine.draft(
                Money.of(new BigDecimal("118.00"), PEN),
                1,
                FIXTURE_IGV_RATE,
                null,
                null,
                Discount.none()
        );
        PricedLine second = PricedLine.draft(
                Money.of(new BigDecimal("100.00"), PEN),
                2,
                FIXTURE_IGV_RATE,
                SalesPolicy.PROMOTION_PERCENT,
                new BigDecimal("10"),
                Discount.none()
        );

        SaleTotals totals = SaleTotals.compute(List.of(first, second), Discount.none());

        assertMoney(new BigDecimal("318.00"), totals.gross());
        assertMoney(new BigDecimal("20.00"), totals.totalDiscount());
        assertMoney(new BigDecimal("298.00"), totals.net());
        assertMoney(new BigDecimal("45.46"), totals.tax());
        assertMoney(new BigDecimal("252.54"), totals.base());
        assertEquals(totals.net(), totals.base().plus(totals.tax()));
    }

    @Test
    void prorratesGlobalFixedDiscountAcrossLines() {
        PricedLine first = PricedLine.draft(
                Money.of(new BigDecimal("100.00"), PEN),
                1,
                BigDecimal.ZERO,
                null,
                null,
                Discount.none()
        );
        PricedLine second = PricedLine.draft(
                Money.of(new BigDecimal("100.00"), PEN),
                1,
                BigDecimal.ZERO,
                null,
                null,
                Discount.none()
        );

        SaleTotals totals = SaleTotals.compute(
                List.of(first, second),
                Discount.of(DiscountKind.FIXED_AMOUNT, new BigDecimal("20.00"))
        );

        assertMoney(new BigDecimal("200.00"), totals.gross());
        assertMoney(new BigDecimal("20.00"), totals.globalDiscount());
        assertMoney(new BigDecimal("180.00"), totals.net());
        assertMoney(BigDecimal.ZERO, totals.tax());
    }

    @Test
    void rejectsEmptyLineList() {
        DomainException error = assertThrows(DomainException.class,
                () -> SaleTotals.compute(List.of(), Discount.none()));

        assertTrue(error.getMessage().toLowerCase().contains("línea"));
    }

    private static void assertMoney(BigDecimal expected, Money actual) {
        assertEquals(0, expected.setScale(Money.SCALE, Money.ROUNDING).compareTo(actual.amount()));
        assertEquals(PEN, actual.currency());
    }
}
