package com.nomos.store.service.model;

public enum SaleTypeEnum {
    BOLETA("Boleta de Venta Electrónica"),
    FACTURA("Factura Electrónica"),
    TICKET("Ticket / Venta Rápida");

    private final String description;

    SaleTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}