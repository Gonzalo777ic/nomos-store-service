package com.valeerp.crm;

import java.math.BigDecimal;

public record OpportunityCreateRequest(
        String name,
        Long partnerId,
        Long leadId,
        Long pipelineId,
        Long stageId,
        BigDecimal expectedAmount,
        String currency,
        String notes
) {
}
