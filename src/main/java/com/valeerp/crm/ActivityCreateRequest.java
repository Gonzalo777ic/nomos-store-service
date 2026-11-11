package com.valeerp.crm;

import java.time.Instant;

public record ActivityCreateRequest(
        String type,
        String subject,
        String notes,
        Instant dueAt
) {
}
