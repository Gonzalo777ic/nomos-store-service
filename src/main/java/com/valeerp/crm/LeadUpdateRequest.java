package com.valeerp.crm;

public record LeadUpdateRequest(
        String title,
        String contactName,
        String email,
        String phone,
        Long partnerId,
        String source,
        String notes
) {
}
