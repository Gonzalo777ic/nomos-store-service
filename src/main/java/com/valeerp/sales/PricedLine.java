package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;

/**
 * Línea de venta valuada en el agregado. Los subtotales del cliente se ignoran.
 */
public record PricedLine(
        Money gross,
        Money promotionDiscount,
        Money lineDiscount,
        Money globalDiscountShare,
        Money net,
        Money tax,
        Money base,
        BigDecimal taxRate
) {

    public static PricedLine draft(
            Money unitPrice,
            int quantity,
            BigDecimal taxRate,
            String promotionType,
            BigDecimal promotionValue,
            Discount explicitLineDiscount
    ) {
        if (quantity <= 0) {
            throw new DomainException("La venta exige una cantidad mayor que cero");
        }
        if (taxRate == null || taxRate.signum() < 0) {
            throw new DomainException("La tasa de impuesto de la línea no es válida");
        }
        Money gross = unitPrice.times(quantity);
        Money promo = promotionDiscountOf(unitPrice, quantity, gross, promotionType, promotionValue);
        Money afterPromo = gross.minus(promo.cappedAt(gross));
        Discount lineDisc = explicitLineDiscount == null ? Discount.none() : explicitLineDiscount;
        Money explicit = lineDisc.applyTo(afterPromo);
        Money remaining = afterPromo.minus(explicit);
        return new PricedLine(
                gross,
                promo.cappedAt(gross),
                explicit,
                Money.zero(unitPrice.currency()),
                remaining,
                Money.zero(unitPrice.currency()),
                Money.zero(unitPrice.currency()),
                taxRate
        );
    }

    /**
     * Compatibilidad con flujos que solo aplican promoción (sin descuento explícito ni global).
     */
    public static PricedLine of(
            Money unitPrice,
            int quantity,
            BigDecimal taxRate,
            String promotionType,
            BigDecimal discountValue
    ) {
        PricedLine draft = draft(unitPrice, quantity, taxRate, promotionType, discountValue, Discount.none());
        return draft.withGlobalShare(Money.zero(unitPrice.currency()));
    }

    public PricedLine withGlobalShare(Money globalShare) {
        Money share = globalShare == null ? Money.zero(net.currency()) : globalShare.cappedAt(net);
        Money adjustedNet = net.minus(share);
        Money tax = adjustedNet.includedTax(taxRate);
        Money base = adjustedNet.minus(tax);
        return new PricedLine(gross, promotionDiscount, lineDiscount, share, adjustedNet, tax, base, taxRate);
    }

    public Money totalDiscount() {
        return promotionDiscount.plus(lineDiscount).plus(globalDiscountShare);
    }

    /** Compatibilidad con tests/flujos que esperan un único descuento de línea. */
    public Money discount() {
        return totalDiscount();
    }

    private static Money promotionDiscountOf(
            Money unitPrice,
            int quantity,
            Money gross,
            String promotionType,
            BigDecimal discountValue
    ) {
        if (promotionType == null) {
            return Money.zero(unitPrice.currency());
        }
        boolean twoForOne = SalesPolicy.PROMOTION_TWO_FOR_ONE.equals(promotionType)
                || "2X1".equals(promotionType)
                || "DOS_POR_UNO".equals(promotionType);
        if (discountValue == null && !twoForOne) {
            return Money.zero(unitPrice.currency());
        }
        return switch (promotionType) {
            case SalesPolicy.PROMOTION_PERCENT -> gross.times(percentRatio(discountValue));
            case SalesPolicy.PROMOTION_FIXED ->
                    Money.of(discountValue.multiply(BigDecimal.valueOf(quantity)), unitPrice.currency());
            case SalesPolicy.PROMOTION_THREE_FOR_TWO -> unitPrice.times(quantity / 3);
            case SalesPolicy.PROMOTION_TWO_FOR_ONE, "2X1", "DOS_POR_UNO" -> unitPrice.times(quantity / 2);
            default -> Money.zero(unitPrice.currency());
        };
    }

    private static BigDecimal percentRatio(BigDecimal value) {
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return value.movePointLeft(2);
        }
        return value;
    }
}
