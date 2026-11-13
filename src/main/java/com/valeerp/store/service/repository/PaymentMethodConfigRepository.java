package com.valeerp.store.service.repository;

import com.valeerp.store.service.model.PaymentMethodConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodConfigRepository extends JpaRepository<PaymentMethodConfig, Long> {

    Optional<PaymentMethodConfig> findByCode(String code);

    boolean existsByCode(String code);
}
