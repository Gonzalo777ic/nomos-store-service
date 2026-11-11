package com.valeerp.sales;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Prorratea un monto Money entre pesos positivos; el remanente cae en la última línea con peso.
 */
public final class MoneyAllocator {

    private MoneyAllocator() {
    }

    public static List<Money> allocate(Money total, List<Money> weights) {
        if (total == null) {
            throw new DomainException("El monto a prorratear es obligatorio");
        }
        if (weights == null || weights.isEmpty()) {
            throw new DomainException("Se requieren pesos para prorratear");
        }
        String currency = total.currency();
        BigDecimal weightSum = BigDecimal.ZERO;
        for (Money weight : weights) {
            if (weight == null) {
                throw new DomainException("Un peso de prorrateo es nulo");
            }
            if (!currency.equals(weight.currency())) {
                throw new DomainException("Los pesos deben compartir moneda con el monto");
            }
            if (weight.isNegative()) {
                throw new DomainException("Los pesos de prorrateo no pueden ser negativos");
            }
            weightSum = weightSum.add(weight.amount());
        }
        List<Money> shares = new ArrayList<>(weights.size());
        if (total.isZero() || weightSum.signum() == 0) {
            for (int i = 0; i < weights.size(); i++) {
                shares.add(Money.zero(currency));
            }
            return shares;
        }
        Money allocated = Money.zero(currency);
        int lastPositive = lastPositiveIndex(weights);
        for (int i = 0; i < weights.size(); i++) {
            if (i == lastPositive) {
                shares.add(total.minus(allocated));
                continue;
            }
            Money weight = weights.get(i);
            if (weight.isZero()) {
                shares.add(Money.zero(currency));
                continue;
            }
            BigDecimal ratio = weight.amount().divide(weightSum, 8, RoundingMode.HALF_UP);
            Money share = total.times(ratio);
            shares.add(share);
            allocated = allocated.plus(share);
        }
        return shares;
    }

    private static int lastPositiveIndex(List<Money> weights) {
        for (int i = weights.size() - 1; i >= 0; i--) {
            if (weights.get(i).isPositive()) {
                return i;
            }
        }
        return weights.size() - 1;
    }
}
