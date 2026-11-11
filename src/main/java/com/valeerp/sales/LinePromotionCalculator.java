package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;

/**
 * Calcula el descuento de una línea según la promoción. Solo se invoca desde el agregado SaleOrder.
 */
public final class LinePromotionCalculator {

    private LinePromotionCalculator() {
    }

    public static Money discountOf(Money unitPrice, int quantity, PromotionSpec promotion) {
        if (promotion == null || promotion.kind() == null) {
            return Money.zero(unitPrice.currency());
        }
        Money gross = unitPrice.times(quantity);
        Money discount = switch (promotion.kind()) {
            case PERCENT -> gross.times(percentRatio(promotion.discountValue()));
            case AMOUNT -> amountDiscount(unitPrice, quantity, promotion);
            case TWO_FOR_ONE -> unitPrice.times(quantity / 2);
        };
        if (discount.amount().compareTo(gross.amount()) > 0) {
            return gross;
        }
        return discount;
    }

    private static Money amountDiscount(Money unitPrice, int quantity, PromotionSpec promotion) {
        if (promotion.currency() != null && !promotion.currency().equalsIgnoreCase(unitPrice.currency())) {
            throw new DomainException("La moneda de la promoción no coincide con la del pedido");
        }
        BigDecimal perUnit = promotion.discountValue() == null ? BigDecimal.ZERO : promotion.discountValue();
        return Money.of(perUnit, unitPrice.currency()).times(quantity);
    }

    private static BigDecimal percentRatio(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return value.movePointLeft(2);
        }
        return value;
    }
}
