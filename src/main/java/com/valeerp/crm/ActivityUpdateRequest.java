package com.valeerp.crm;

import java.time.Instant;

public record ActivityUpdateRequest(
        String subject,
        String notes,
        Instant dueAt
) {
}
