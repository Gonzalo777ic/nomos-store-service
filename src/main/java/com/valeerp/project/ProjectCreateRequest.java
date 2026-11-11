package com.valeerp.project;

import java.math.BigDecimal;

public record ProjectCreateRequest(
        String code,
        String name,
        String description,
        Long partnerId,
        Long managerUserId,
        BigDecimal plannedBudget,
        String currency
) {
}
