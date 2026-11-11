package com.valeerp.crm;

import java.math.BigDecimal;

public record OpportunityUpdateRequest(
        String name,
        String notes,
        BigDecimal expectedAmount,
        String currency
) {
}
