package com.valeerp.project;

import java.math.BigDecimal;
import java.time.Instant;

public record ProjectResponse(
        Long id,
        String code,
        String name,
        String description,
        Long partnerId,
        Long managerUserId,
        String status,
        BigDecimal plannedBudget,
        String currency,
        BigDecimal plannedTaskHours,
        BigDecimal confirmedHours,
        BigDecimal confirmedBillableAmount,
        Instant createdAt,
        Instant closedAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getPartnerId(),
                project.getManagerUserId(),
                project.getStatus().name(),
                project.getPlannedBudget(),
                project.getCurrency(),
                project.plannedTaskHours(),
                project.confirmedHours(),
                project.confirmedBillableTotal().amount(),
                project.getCreatedAt(),
                project.getClosedAt()
        );
    }
}
