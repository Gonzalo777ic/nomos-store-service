package com.valeerp.partner;

public record PartnerContactRequest(
        String fullName,
        String email,
        String phone,
        String roleLabel
) {
}
