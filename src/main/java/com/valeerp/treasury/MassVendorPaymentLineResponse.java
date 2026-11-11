package com.valeerp.treasury;

public record MassVendorPaymentLineResponse(
        Long id,
        Long partnerId,
        Long payableDocumentId,
        MoneyDto amount,
        String concept
) {

    public static MassVendorPaymentLineResponse from(MassVendorPaymentLine line) {
        return new MassVendorPaymentLineResponse(
                line.getId(),
                line.getPartnerId(),
                line.getPayableDocumentId(),
                MoneyDto.from(line.amount()),
                line.getConcept()
        );
    }
}
