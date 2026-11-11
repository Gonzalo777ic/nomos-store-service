package com.valeerp.partner;

public record PartnerAddressRequest(
        String kind,
        String line,
        String city,
        String region,
        String postalCode,
        String country,
        Boolean primary
) {
}
