package com.nomos.store.service.model;

public enum PaymentConditionEnum {
    CONTADO("Pago al Contado"),
    CREDITO("Pago a Crédito");

    private final String description;

    PaymentConditionEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}