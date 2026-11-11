package com.valeerp.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogPromotionRepository extends JpaRepository<CatalogPromotion, Long> {

    Optional<CatalogPromotion> findByCode(String code);

    boolean existsByCode(String code);

    Page<CatalogPromotion> findAll(Pageable pageable);
}
