package com.valeerp.sales;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * #68 — totales de línea, promociones e impuesto incluido con Money y fixtures locales.
 */
class PricedLineTest {

    private static final String PEN = Currency.PEN.code();
    /** Fixture local de test (regla: tests pueden usar literales). */
    private static final BigDecimal FIXTURE_IGV_RATE = new BigDecimal("0.18");

    @Test
    void pricesLineWithoutPromotionAndSplitsIncludedTax() {
        Money unit = Money.of(new BigDecimal("118.00"), PEN);

        PricedLine line = PricedLine.of(unit, 1, FIXTURE_IGV_RATE, null, null);

        assertMoney(new BigDecimal("118.00"), line.gross());
        assertMoney(BigDecimal.ZERO, line.discount());
        assertMoney(new BigDecimal("118.00"), line.net());
        assertMoney(new BigDecimal("18.00"), line.tax());
        assertMoney(new BigDecimal("100.00"), line.base());
        assertEquals(line.net(), line.base().plus(line.tax()));
    }

    @Test
    void appliesPercentPromotionBeforeTax() {
        Money unit = Money.of(new BigDecimal("100.00"), PEN);

        PricedLine line = PricedLine.of(
                unit, 2, FIXTURE_IGV_RATE, SalesPolicy.PROMOTION_PERCENT, new BigDecimal("10"));

        assertMoney(new BigDecimal("200.00"), line.gross());
        assertMoney(new BigDecimal("20.00"), line.discount());
        assertMoney(new BigDecimal("180.00"), line.net());
        assertMoney(new BigDecimal("27.46"), line.tax());
        assertMoney(new BigDecimal("152.54"), line.base());
    }

    @Test
    void appliesFixedAmountPromotionPerUnit() {
        Money unit = Money.of(new BigDecimal("50.00"), PEN);

        PricedLine line = PricedLine.of(
                unit, 3, BigDecimal.ZERO, SalesPolicy.PROMOTION_FIXED, new BigDecimal("5.00"));

        assertMoney(new BigDecimal("150.00"), line.gross());
        assertMoney(new BigDecimal("15.00"), line.discount());
        assertMoney(new BigDecimal("135.00"), line.net());
        assertMoney(BigDecimal.ZERO, line.tax());
        assertMoney(new BigDecimal("135.00"), line.base());
    }

    @Test
    void appliesThreeForTwoPromotion() {
        Money unit = Money.of(new BigDecimal("40.00"), PEN);

        PricedLine line = PricedLine.of(
                unit, 5, BigDecimal.ZERO, SalesPolicy.PROMOTION_THREE_FOR_TWO, BigDecimal.ONE);

        assertMoney(new BigDecimal("200.00"), line.gross());
        assertMoney(new BigDecimal("40.00"), line.discount());
        assertMoney(new BigDecimal("160.00"), line.net());
    }

    @Test
    void capsDiscountAtGross() {
        Money unit = Money.of(new BigDecimal("10.00"), PEN);

        PricedLine line = PricedLine.of(
                unit, 1, BigDecimal.ZERO, SalesPolicy.PROMOTION_FIXED, new BigDecimal("50.00"));

        assertMoney(new BigDecimal("10.00"), line.gross());
        assertMoney(new BigDecimal("10.00"), line.discount());
        assertMoney(BigDecimal.ZERO, line.net());
    }

    @Test
    void rejectsNonPositiveQuantity() {
        Money unit = Money.of(new BigDecimal("10.00"), PEN);

        DomainException error = assertThrows(DomainException.class,
                () -> PricedLine.of(unit, 0, FIXTURE_IGV_RATE, null, null));

        assertTrue(error.getMessage().contains("cantidad"));
    }

    private static void assertMoney(BigDecimal expected, Money actual) {
        assertEquals(0, expected.setScale(Money.SCALE, Money.ROUNDING).compareTo(actual.amount()));
        assertEquals(PEN, actual.currency());
    }
}
