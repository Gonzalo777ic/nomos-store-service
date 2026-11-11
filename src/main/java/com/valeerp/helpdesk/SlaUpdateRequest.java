package com.valeerp.helpdesk;

public record SlaUpdateRequest(
        String code,
        String name,
        String description,
        Integer responseHours,
        Integer resolutionHours
) {
}
