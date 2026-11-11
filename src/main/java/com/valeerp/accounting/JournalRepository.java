package com.valeerp.accounting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    List<Journal> findByCompanyIdOrderByCodeAsc(Long companyId);

    Page<Journal> findByCompanyId(Long companyId, Pageable pageable);

    Page<Journal> findByCompanyIdAndKind(Long companyId, JournalKind kind, Pageable pageable);

    Optional<Journal> findByCompanyIdAndCode(Long companyId, String code);

    List<Journal> findByCompanyIdAndKindOrderByCodeAsc(Long companyId, JournalKind kind);
}
