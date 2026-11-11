package com.valeerp.sales;

/**
 * Ciclo de vida de la cotización / pedido de venta. Confirmación y cancelación por dominio.
 */
public enum SaleOrderStatus {
    DRAFT,
    CONFIRMED,
    CANCELLED;

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
}
