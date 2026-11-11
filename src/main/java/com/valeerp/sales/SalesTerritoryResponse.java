package com.valeerp.sales;

public record SalesTerritoryResponse(Long id, String code, String name, String region, String description) {

    public static SalesTerritoryResponse from(SalesTerritory territory) {
        return new SalesTerritoryResponse(
                territory.getId(), territory.getCode(), territory.getName(),
                territory.getRegion(), territory.getDescription());
    }
}
