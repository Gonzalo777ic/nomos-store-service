package com.valeerp.partner;

public record PartnerUpdateRequest(
        String partnerType,
        String name,
        String tradeName,
        String documentType,
        String documentNumber,
        String email,
        String phone
) {
}
