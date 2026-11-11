package com.valeerp.crm;

import java.util.List;

public record PipelineBoardResponse(
        PipelineResponse pipeline,
        List<PipelineBoardColumnResponse> columns
) {
}
