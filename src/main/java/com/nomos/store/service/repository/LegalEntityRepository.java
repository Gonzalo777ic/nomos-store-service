package com.nomos.store.service.repository;

import com.nomos.store.service.model.LegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LegalEntityRepository extends JpaRepository<LegalEntity, Long> {
    Optional<LegalEntity> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);
}