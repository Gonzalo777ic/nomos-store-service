package com.valeerp.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChartAccountRepository extends JpaRepository<ChartAccount, Long> {

    Page<ChartAccount> findByCompanyId(Long companyId, Pageable pageable);

    Page<ChartAccount> findByCompanyIdAndParentIsNull(Long companyId, Pageable pageable);

    List<ChartAccount> findByCompanyIdAndParentId(Long companyId, Long parentId);

    Optional<ChartAccount> findByCompanyIdAndCode(Long companyId, String code);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}
