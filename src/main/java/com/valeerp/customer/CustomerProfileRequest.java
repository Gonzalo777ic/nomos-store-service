package com.valeerp.customer;

public record CustomerProfileRequest(
        String auth0Id,
        String email,
        String fullName,
        String documentType,
        String documentNumber,
        String phone,
        String address
) {
}
