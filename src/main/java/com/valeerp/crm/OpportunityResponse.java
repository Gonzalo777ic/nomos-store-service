package com.valeerp.crm;

import java.math.BigDecimal;
import java.time.Instant;

public record OpportunityResponse(
        Long id,
        String name,
        Long partnerId,
        Long leadId,
        Long pipelineId,
        Long stageId,
        String status,
        BigDecimal expectedAmount,
        String currency,
        String notes,
        String lostReason,
        Instant createdAt,
        Instant closedAt
) {

    public static OpportunityResponse from(CrmOpportunity opportunity) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getName(),
                opportunity.getPartnerId(),
                opportunity.getLeadId(),
                opportunity.getPipelineId(),
                opportunity.getStageId(),
                opportunity.getStatus().name(),
                opportunity.getExpectedAmount(),
                opportunity.getCurrency(),
                opportunity.getNotes(),
                opportunity.getLostReason(),
                opportunity.getCreatedAt(),
                opportunity.getClosedAt()
        );
    }
}
