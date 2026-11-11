package com.valeerp.project;

import java.math.BigDecimal;
import java.time.Instant;

public record TaskResponse(
        Long id,
        Long projectId,
        String name,
        String description,
        String status,
        Long assigneeUserId,
        BigDecimal plannedHours,
        Instant createdAt,
        Instant closedAt
) {

    public static TaskResponse from(ProjectTask task) {
        return new TaskResponse(
                task.getId(),
                task.getProjectId(),
                task.getName(),
                task.getDescription(),
                task.getStatus().name(),
                task.getAssigneeUserId(),
                task.getPlannedHours(),
                task.getCreatedAt(),
                task.getClosedAt()
        );
    }
}
