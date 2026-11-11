package com.valeerp.partner;

import java.time.Instant;
import java.util.List;

public record PartnerResponse(
        Long id,
        String partnerType,
        String name,
        String tradeName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        Instant createdAt,
        PartnerCategoryResponse category,
        LocalPriceListRefResponse priceList,
        PaymentTermResponse paymentTerm,
        MoneyView creditLimit,
        List<PartnerAddressResponse> addresses,
        List<PartnerContactResponse> contacts,
        List<String> tags,
        List<PartnerBankAccountResponse> bankAccounts
) {

    public static PartnerResponse from(Partner partner) {
        return new PartnerResponse(
                partner.getId(),
                partner.getPartnerType().name(),
                partner.getName(),
                partner.getTradeName(),
                partner.getDocumentType().name(),
                partner.getDocumentNumber(),
                partner.getEmail(),
                partner.getPhone(),
                partner.getCreatedAt(),
                partner.getCategory() == null ? null : PartnerCategoryResponse.from(partner.getCategory()),
                partner.getPriceListRef() == null ? null : LocalPriceListRefResponse.from(partner.getPriceListRef()),
                partner.getPaymentTerm() == null ? null : PaymentTermResponse.from(partner.getPaymentTerm()),
                MoneyView.from(partner.creditLimit()),
                partner.activeAddresses().stream().map(PartnerAddressResponse::from).toList(),
                partner.activeContacts().stream().map(PartnerContactResponse::from).toList(),
                partner.activeTags().stream().map(PartnerTag::getLabel).toList(),
                partner.activeBankAccounts().stream().map(PartnerBankAccountResponse::from).toList()
        );
    }
}
