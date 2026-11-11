package com.valeerp.crm;

import java.math.BigDecimal;

public record LeadConvertRequest(
        Long pipelineId,
        Long stageId,
        String opportunityName,
        BigDecimal expectedAmount,
        String currency,
        String notes
) {
}
