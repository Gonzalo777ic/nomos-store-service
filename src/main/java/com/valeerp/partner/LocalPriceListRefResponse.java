package com.valeerp.partner;

public record LocalPriceListRefResponse(Long id, Long catalogPriceListId, String code, String name) {

    public static LocalPriceListRefResponse from(LocalPriceListRef ref) {
        return new LocalPriceListRefResponse(
                ref.getId(),
                ref.getCatalogPriceListId(),
                ref.getCode(),
                ref.getName()
        );
    }
}
