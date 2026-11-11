package com.valeerp.sales;

import java.math.BigDecimal;

public record CommissionRuleRequest(String code, String name, BigDecimal percentRate, String description) {
}
