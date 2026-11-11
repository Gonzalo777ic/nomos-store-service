package com.valeerp.partner;

public record PartnerBankAccountRequest(
        String bankCode,
        String bankName,
        String accountNumber,
        String cci,
        String currencyCode,
        String holderName,
        String purpose,
        Boolean primary
) {
}
