package com.valeerp.treasury;

import com.valeerp.kernel.Money;

import java.math.BigDecimal;

public record MoneyDto(BigDecimal amount, String currency) {

    public static MoneyDto from(Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyDto(money.amount(), money.currency());
    }

    public Money toMoney() {
        return Money.of(amount, currency == null || currency.isBlank()
                ? TreasuryPolicy.DEFAULT_CURRENCY.code()
                : currency);
    }

    public Money toMoneyOrDefault() {
        if (amount == null) {
            return Money.zero(TreasuryPolicy.DEFAULT_CURRENCY.code());
        }
        return toMoney();
    }
}
