package com.valeerp.crm;

public record PipelineUpdateRequest(
        String code,
        String name,
        String description
) {
}
