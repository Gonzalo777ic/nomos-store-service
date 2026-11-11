package com.valeerp.helpdesk;

public record SlaCreateRequest(
        String code,
        String name,
        String description,
        Integer responseHours,
        Integer resolutionHours
) {
}
