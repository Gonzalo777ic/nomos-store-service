package com.valeerp.accounting;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "valeerp.ledger")
public record LedgerProperties(
        String receivableCode,
        String receivableName,
        String taxCode,
        String taxName,
        String revenueCode,
        String revenueName
) {
}
