package com.valeerp.partner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {

    Optional<PaymentTerm> findByCode(String code);

    boolean existsByCode(String code);
}
