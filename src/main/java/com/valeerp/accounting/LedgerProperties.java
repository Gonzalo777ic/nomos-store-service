package com.valeerp.accounting;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Códigos de cuenta del ledger local (despliegue). No hardcodear en servicios de dominio.
 */
@ConfigurationProperties(prefix = "valeerp.ledger")
public record LedgerProperties(
        String receivableCode,
        String receivableName,
        String payableCode,
        String payableName,
        String taxCode,
        String taxName,
        String revenueCode,
        String revenueName,
        String expenseCode,
        String expenseName
) {
}
