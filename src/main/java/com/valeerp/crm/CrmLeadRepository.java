package com.valeerp.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {

    Page<CrmLead> findAll(Pageable pageable);

    Page<CrmLead> findByStatus(LeadStatus status, Pageable pageable);

    Page<CrmLead> findByPartnerId(Long partnerId, Pageable pageable);
}
