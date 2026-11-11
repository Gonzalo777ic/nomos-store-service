package com.valeerp.partner;

public record PartnerCategoryResponse(Long id, String code, String name, String description) {

    public static PartnerCategoryResponse from(PartnerCategory category) {
        return new PartnerCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription()
        );
    }
}
