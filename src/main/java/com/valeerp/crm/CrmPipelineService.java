package com.valeerp.crm;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmPipelineService {

    private final CrmPipelineRepository pipelineRepository;
    private final CrmOpportunityRepository opportunityRepository;
    private final TimeProvider timeProvider;

    public CrmPipelineService(CrmPipelineRepository pipelineRepository,
                              CrmOpportunityRepository opportunityRepository,
                              TimeProvider timeProvider) {
        this.pipelineRepository = pipelineRepository;
        this.opportunityRepository = opportunityRepository;
        this.timeProvider = timeProvider;
    }

    public Page<CrmPipeline> list(Pageable pageable) {
        return pipelineRepository.findAll(pageable);
    }

    public CrmPipeline require(Long id) {
        return pipelineRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Pipeline no encontrado"));
    }

    @Transactional
    public CrmPipeline create(PipelineCreateRequest request) {
        ensureCodeAvailable(request.code(), null);
        CrmPipeline pipeline = CrmPipeline.open(
                request.code(),
                request.name(),
                request.description(),
                timeProvider.now()
        );
        applyStages(pipeline, request.stages());
        return pipelineRepository.save(pipeline);
    }

    @Transactional
    public CrmPipeline update(Long id, PipelineUpdateRequest request) {
        CrmPipeline pipeline = require(id);
        ensureCodeAvailable(request.code(), id);
        pipeline.rename(request.code(), request.name(), request.description());
        return pipelineRepository.save(pipeline);
    }

    @Transactional
    public void retire(Long id) {
        CrmPipeline pipeline = require(id);
        pipeline.archive(Actors.currentActor(), timeProvider.now());
        pipelineRepository.save(pipeline);
    }

    @Transactional
    public CrmPipelineStage addStage(Long pipelineId, StageCreateRequest request) {
        CrmPipeline pipeline = require(pipelineId);
        CrmPipelineStage stage = pipeline.addStage(
                request.code(),
                request.name(),
                request.sequenceNo(),
                StageOutcome.require(request.outcome())
        );
        pipelineRepository.save(pipeline);
        return stage;
    }

    @Transactional
    public CrmPipelineStage updateStage(Long pipelineId, Long stageId, StageUpdateRequest request) {
        CrmPipeline pipeline = require(pipelineId);
        pipeline.updateStage(
                stageId,
                request.code(),
                request.name(),
                request.sequenceNo(),
                StageOutcome.require(request.outcome())
        );
        pipelineRepository.save(pipeline);
        return pipeline.requireActiveStage(stageId);
    }

    @Transactional
    public void retireStage(Long pipelineId, Long stageId) {
        CrmPipeline pipeline = require(pipelineId);
        long openOnStage = opportunityRepository.countByPipelineIdAndStageIdAndStatus(
                pipelineId, stageId, OpportunityStatus.OPEN);
        if (openOnStage > 0) {
            throw new DomainException("No se puede dar de baja una etapa con oportunidades abiertas");
        }
        pipeline.retireStage(stageId, Actors.currentActor(), timeProvider.now());
        pipelineRepository.save(pipeline);
    }

    public PipelineBoardResponse board(Long pipelineId) {
        CrmPipeline pipeline = require(pipelineId);
        List<CrmOpportunity> open = opportunityRepository
                .findByPipelineIdAndStatusOrderByCreatedAtAsc(pipelineId, OpportunityStatus.OPEN);
        Map<Long, List<CrmOpportunity>> byStage = open.stream()
                .collect(Collectors.groupingBy(CrmOpportunity::getStageId));

        List<PipelineBoardColumnResponse> columns = new ArrayList<>();
        for (CrmPipelineStage stage : pipeline.activeStages()) {
            List<OpportunityResponse> cards = byStage.getOrDefault(stage.getId(), List.of()).stream()
                    .map(OpportunityResponse::from)
                    .toList();
            columns.add(new PipelineBoardColumnResponse(StageResponse.from(stage), cards));
        }
        return new PipelineBoardResponse(PipelineResponse.from(pipeline), columns);
    }

    private void applyStages(CrmPipeline pipeline, List<StageCreateRequest> stages) {
        if (stages == null || stages.isEmpty()) {
            return;
        }
        for (StageCreateRequest request : stages) {
            pipeline.addStage(
                    request.code(),
                    request.name(),
                    request.sequenceNo(),
                    StageOutcome.require(request.outcome())
            );
        }
    }

    private void ensureCodeAvailable(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de pipeline es obligatorio");
        }
        String normalized = code.trim().toUpperCase();
        pipelineRepository.findByCode(normalized)
                .filter(existing -> excludeId == null || !excludeId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe un pipeline con código " + normalized);
                });
    }
}
