package com.nomos.store.service.model;

public enum SaleTypeEnum {
    BOLETA("Boleta de Venta Electrónica"),
    FACTURA("Factura Electrónica"),
    TICKET("Ticket / Venta Rápida"); // Mejor que "OTRO"

    private final String description;

    SaleTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}