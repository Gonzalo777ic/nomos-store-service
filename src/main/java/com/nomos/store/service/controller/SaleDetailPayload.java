package com.nomos.store.service.controller;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Payload para recibir un solo ítem de detalle de venta desde el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailPayload {
    private Long productId;
    private Double unitPrice;
    private Integer quantity;

    private Double subtotal;
    private Long taxRateId;
    private Long promotionId;
}