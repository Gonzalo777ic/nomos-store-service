package com.valeerp.project;

import java.math.BigDecimal;

public record TaskUpdateRequest(
        String name,
        String description,
        Long assigneeUserId,
        BigDecimal plannedHours
) {
}
