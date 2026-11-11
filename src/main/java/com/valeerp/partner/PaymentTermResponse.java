package com.valeerp.partner;

public record PaymentTermResponse(Long id, String code, String name, int daysDue, String description) {

    public static PaymentTermResponse from(PaymentTerm term) {
        return new PaymentTermResponse(
                term.getId(),
                term.getCode(),
                term.getName(),
                term.getDaysDue(),
                term.getDescription()
        );
    }
}
