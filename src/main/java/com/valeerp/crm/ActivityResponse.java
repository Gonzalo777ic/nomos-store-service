package com.valeerp.crm;

import java.time.Instant;

public record ActivityResponse(
        Long id,
        Long opportunityId,
        String type,
        String status,
        String subject,
        String notes,
        Instant dueAt,
        Instant createdAt,
        Instant completedAt,
        Instant cancelledAt,
        String cancelReason
) {

    public static ActivityResponse from(CrmActivity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getOpportunityId(),
                activity.getType().name(),
                activity.getStatus().name(),
                activity.getSubject(),
                activity.getNotes(),
                activity.getDueAt(),
                activity.getCreatedAt(),
                activity.getCompletedAt(),
                activity.getCancelledAt(),
                activity.getCancelReason()
        );
    }
}
