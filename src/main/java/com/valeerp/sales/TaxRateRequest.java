package com.valeerp.sales;

import java.math.BigDecimal;

public record TaxRateRequest(String name, String kind, BigDecimal rate) {
}
