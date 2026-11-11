package com.valeerp.crm;

public record StageResponse(
        Long id,
        Long pipelineId,
        String code,
        String name,
        int sequenceNo,
        String outcome
) {

    public static StageResponse from(CrmPipelineStage stage) {
        return new StageResponse(
                stage.getId(),
                stage.getPipelineId(),
                stage.getCode(),
                stage.getName(),
                stage.getSequenceNo(),
                stage.getOutcome().name()
        );
    }
}
