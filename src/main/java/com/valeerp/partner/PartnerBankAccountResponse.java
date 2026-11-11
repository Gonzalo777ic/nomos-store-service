package com.valeerp.partner;

public record PartnerBankAccountResponse(
        Long id,
        String bankCode,
        String bankName,
        String accountNumber,
        String cci,
        String currencyCode,
        String holderName,
        String purpose,
        boolean primary
) {

    public static PartnerBankAccountResponse from(PartnerBankAccount account) {
        return new PartnerBankAccountResponse(
                account.getId(),
                account.getBankCode(),
                account.getBankName(),
                account.getAccountNumber(),
                account.getCci(),
                account.getCurrencyCode(),
                account.getHolderName(),
                account.getPurpose().name(),
                account.isPrimaryAccount()
        );
    }
}
