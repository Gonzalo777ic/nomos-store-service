package com.valeerp.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesTerritoryRepository extends JpaRepository<SalesTerritory, Long> {
    Optional<SalesTerritory> findByCode(String code);
}
