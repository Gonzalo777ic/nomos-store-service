package com.valeerp.customer;

public record CustomerResponse(
        Long id,
        String auth0Id,
        String email,
        String fullName,
        String documentType,
        String documentNumber,
        String phone,
        String address
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getAuth0Id(),
                customer.getEmail(),
                customer.getFullName(),
                customer.getDocumentType(),
                customer.getDocumentNumber(),
                customer.getPhone(),
                customer.getAddress()
        );
    }
}
