package com.valeerp.crm;

public record StageUpdateRequest(
        String code,
        String name,
        Integer sequenceNo,
        String outcome
) {
}
