package com.valeerp.partner;

import java.util.List;

public record PartnerCreateRequest(
        String partnerType,
        String name,
        String tradeName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        List<PartnerAddressRequest> addresses,
        List<PartnerContactRequest> contacts,
        List<String> tags
) {
}
