package com.valeerp.sales;

import java.math.BigDecimal;

public record CommissionRuleResponse(Long id, String code, String name, BigDecimal percentRate, String description) {

    public static CommissionRuleResponse from(CommissionRule rule) {
        return new CommissionRuleResponse(
                rule.getId(), rule.getCode(), rule.getName(), rule.getPercentRate(), rule.getDescription());
    }
}
