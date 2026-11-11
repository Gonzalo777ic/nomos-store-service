package com.valeerp.helpdesk;

import java.time.Instant;

public record SlaResponse(
        Long id,
        String code,
        String name,
        String description,
        int responseHours,
        int resolutionHours,
        Instant createdAt
) {

    public static SlaResponse from(HelpdeskSla sla) {
        return new SlaResponse(
                sla.getId(),
                sla.getCode(),
                sla.getName(),
                sla.getDescription(),
                sla.getResponseHours(),
                sla.getResolutionHours(),
                sla.getCreatedAt()
        );
    }
}
