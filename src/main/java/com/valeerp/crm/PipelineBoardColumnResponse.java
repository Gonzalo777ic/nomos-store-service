package com.valeerp.crm;

import java.util.List;

public record PipelineBoardColumnResponse(
        StageResponse stage,
        List<OpportunityResponse> opportunities
) {
}
