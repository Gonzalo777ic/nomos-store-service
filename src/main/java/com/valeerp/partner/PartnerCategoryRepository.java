package com.valeerp.partner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerCategoryRepository extends JpaRepository<PartnerCategory, Long> {

    Optional<PartnerCategory> findByCode(String code);

    boolean existsByCode(String code);
}
