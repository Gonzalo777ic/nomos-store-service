package com.valeerp.partner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalPriceListRefRepository extends JpaRepository<LocalPriceListRef, Long> {

    Optional<LocalPriceListRef> findByCode(String code);

    boolean existsByCode(String code);
}
