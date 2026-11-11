package com.valeerp.partner;

public record PartnerContactResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String roleLabel
) {

    public static PartnerContactResponse from(PartnerContact contact) {
        return new PartnerContactResponse(
                contact.getId(),
                contact.getFullName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getRoleLabel()
        );
    }
}
