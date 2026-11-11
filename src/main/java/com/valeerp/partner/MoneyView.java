package com.valeerp.partner;

import com.valeerp.kernel.Money;

import java.math.BigDecimal;

public record MoneyView(BigDecimal amount, String currency) {

    public static MoneyView from(Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyView(money.amount(), money.currency());
    }

    public Money toMoney() {
        return Money.of(amount, currency);
    }
}
