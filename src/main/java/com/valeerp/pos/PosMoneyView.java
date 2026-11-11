package com.valeerp.pos;

import com.valeerp.kernel.Money;

import java.math.BigDecimal;

public record PosMoneyView(BigDecimal amount, String currency) {

    public static PosMoneyView from(Money money) {
        if (money == null) {
            return null;
        }
        return new PosMoneyView(money.amount(), money.currency());
    }

    public Money toMoney() {
        return Money.of(amount, currency);
    }
}
