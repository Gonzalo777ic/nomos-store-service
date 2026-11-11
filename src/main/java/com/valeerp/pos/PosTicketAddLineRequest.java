package com.valeerp.pos;

import java.math.BigDecimal;

/**
 * El cliente puede enviar unitPrice de la lista; cualquier subtotal/total del cliente se ignora.
 */
public record PosTicketAddLineRequest(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        Long taxRateId
) {
}
