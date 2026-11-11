package com.valeerp.crm;

public record LeadCreateRequest(
        String title,
        String contactName,
        String email,
        String phone,
        Long partnerId,
        String source,
        String notes
) {
}
