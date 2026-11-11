package com.valeerp.crm;

import java.time.Instant;
import java.util.List;

public record PipelineResponse(
        Long id,
        String code,
        String name,
        String description,
        Instant createdAt,
        List<StageResponse> stages
) {

    public static PipelineResponse from(CrmPipeline pipeline) {
        return new PipelineResponse(
                pipeline.getId(),
                pipeline.getCode(),
                pipeline.getName(),
                pipeline.getDescription(),
                pipeline.getCreatedAt(),
                pipeline.activeStages().stream().map(StageResponse::from).toList()
        );
    }
}
