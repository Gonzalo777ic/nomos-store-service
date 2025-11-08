package com.nomos.store.service.controller; // En el mismo paquete del Controller

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

    private Double subtotal; // Usado para cálculos preliminares del frontend
    private Long taxRateId;
    private Long promotionId; // Puede ser nulo
}