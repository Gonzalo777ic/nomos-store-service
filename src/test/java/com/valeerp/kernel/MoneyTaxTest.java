package com.valeerp.kernel;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTaxTest {

    private static final String PEN = Currency.PEN.code();
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Test
    void extractsIncludedTaxFromGrossAmount() {
        Money gross = Money.of(new BigDecimal("118.00"), PEN);

        Money tax = gross.includedTax(IGV_RATE);

        assertEquals(0, new BigDecimal("18.00").compareTo(tax.amount()));
        assertEquals(PEN, tax.currency());
        assertEquals(0, new BigDecimal("100.00").compareTo(gross.minus(tax).amount()));
    }

    @Test
    void zeroRateYieldsZeroTax() {
        Money gross = Money.of(new BigDecimal("50.00"), PEN);

        Money tax = gross.includedTax(BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(tax.amount()));
    }

    @Test
    void rejectsNegativeTaxRate() {
        Money gross = Money.of(new BigDecimal("10.00"), PEN);

        assertThrows(DomainException.class, () -> gross.includedTax(new BigDecimal("-0.01")));
    }
}
