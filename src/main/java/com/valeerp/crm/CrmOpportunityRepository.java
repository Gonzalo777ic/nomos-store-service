package com.valeerp.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmOpportunityRepository extends JpaRepository<CrmOpportunity, Long> {

    Page<CrmOpportunity> findAll(Pageable pageable);

    Page<CrmOpportunity> findByPipelineId(Long pipelineId, Pageable pageable);

    Page<CrmOpportunity> findByPipelineIdAndStatus(Long pipelineId, OpportunityStatus status, Pageable pageable);

    Page<CrmOpportunity> findByStageId(Long stageId, Pageable pageable);

    Page<CrmOpportunity> findByPartnerId(Long partnerId, Pageable pageable);

    Page<CrmOpportunity> findByStatus(OpportunityStatus status, Pageable pageable);

    List<CrmOpportunity> findByPipelineIdAndStatusOrderByCreatedAtAsc(Long pipelineId, OpportunityStatus status);

    long countByPipelineIdAndStageIdAndStatus(Long pipelineId, Long stageId, OpportunityStatus status);
}
