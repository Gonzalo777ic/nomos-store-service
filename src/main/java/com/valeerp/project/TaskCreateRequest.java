package com.valeerp.project;

import java.math.BigDecimal;

public record TaskCreateRequest(
        String name,
        String description,
        Long assigneeUserId,
        BigDecimal plannedHours
) {
}
