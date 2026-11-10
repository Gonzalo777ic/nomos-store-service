package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;

public record PricedLine(Money gross, Money discount, Money net, Money tax, Money base) {

    public static PricedLine of(Money unitPrice, int quantity, BigDecimal taxRate, String promotionType, BigDecimal discountValue) {
        if (quantity <= 0) {
            throw new DomainException("La venta exige una cantidad mayor que cero");
        }
        Money gross = unitPrice.times(quantity);
        Money discount = discountOf(unitPrice, quantity, gross, promotionType, discountValue);
        if (discount.amount().compareTo(gross.amount()) > 0) {
            discount = gross;
        }
        Money net = gross.minus(discount);
        Money tax = net.includedTax(taxRate == null ? BigDecimal.ZERO : taxRate);
        Money base = net.minus(tax);
        return new PricedLine(gross, discount, net, tax, base);
    }

    private static Money discountOf(Money unitPrice, int quantity, Money gross, String promotionType, BigDecimal discountValue) {
        if (promotionType == null || discountValue == null) {
            return Money.zero(unitPrice.currency());
        }
        return switch (promotionType) {
            case "PORCENTAJE" -> gross.times(ratio(discountValue));
            case "MONTO_FIJO" -> Money.of(discountValue.multiply(BigDecimal.valueOf(quantity)), unitPrice.currency());
            case "TRES_POR_DOS" -> unitPrice.times(quantity / 3);
            default -> Money.zero(unitPrice.currency());
        };
    }

    private static BigDecimal ratio(BigDecimal value) {
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return value.movePointLeft(2);
        }
        return value;
    }
}
