package com.valeerp.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmActivityRepository extends JpaRepository<CrmActivity, Long> {

    List<CrmActivity> findByOpportunityIdOrderByCreatedAtDesc(Long opportunityId);

    Page<CrmActivity> findByOpportunityId(Long opportunityId, Pageable pageable);

    Page<CrmActivity> findByOpportunityIdAndStatus(Long opportunityId, ActivityStatus status, Pageable pageable);

    Page<CrmActivity> findByStatus(ActivityStatus status, Pageable pageable);
}
