package com.valeerp.pos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PosConfigRepository extends JpaRepository<PosConfig, Long> {

    Page<PosConfig> findByCompanyId(Long companyId, Pageable pageable);

    Optional<PosConfig> findByCompanyIdAndCode(Long companyId, String code);
}
