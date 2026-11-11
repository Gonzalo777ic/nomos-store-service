package com.valeerp.partner;

public record PartnerAddressResponse(
        Long id,
        String kind,
        String line,
        String city,
        String region,
        String postalCode,
        String country,
        boolean primary
) {

    public static PartnerAddressResponse from(PartnerAddress address) {
        return new PartnerAddressResponse(
                address.getId(),
                address.getKind().name(),
                address.getLine(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountry(),
                address.isPrimaryAddress()
        );
    }
}
