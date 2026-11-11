package com.valeerp.crm;

import java.util.List;

public record PipelineCreateRequest(
        String code,
        String name,
        String description,
        List<StageCreateRequest> stages
) {
}
