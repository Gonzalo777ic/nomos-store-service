package com.valeerp.crm;

public record StageCreateRequest(
        String code,
        String name,
        Integer sequenceNo,
        String outcome
) {
}
