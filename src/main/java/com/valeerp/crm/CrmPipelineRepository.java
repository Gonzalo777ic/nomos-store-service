package com.valeerp.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmPipelineRepository extends JpaRepository<CrmPipeline, Long> {

    Page<CrmPipeline> findAll(Pageable pageable);

    Optional<CrmPipeline> findByCode(String code);

    boolean existsByCode(String code);
}
