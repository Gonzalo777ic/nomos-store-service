package com.valeerp.crm;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.PartnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class CrmOpportunityService {

    private final CrmOpportunityRepository opportunityRepository;
    private final CrmPipelineService pipelineService;
    private final PartnerService partnerService;
    private final TimeProvider timeProvider;

    public CrmOpportunityService(CrmOpportunityRepository opportunityRepository,
                                 CrmPipelineService pipelineService,
                                 PartnerService partnerService,
                                 TimeProvider timeProvider) {
        this.opportunityRepository = opportunityRepository;
        this.pipelineService = pipelineService;
        this.partnerService = partnerService;
        this.timeProvider = timeProvider;
    }

    public Page<CrmOpportunity> list(Long pipelineId, Long stageId, Long partnerId,
                                     OpportunityStatus status, Pageable pageable) {
        if (stageId != null) {
            return opportunityRepository.findByStageId(stageId, pageable);
        }
        if (partnerId != null) {
            return opportunityRepository.findByPartnerId(partnerId, pageable);
        }
        if (pipelineId != null && status != null) {
            return opportunityRepository.findByPipelineIdAndStatus(pipelineId, status, pageable);
        }
        if (pipelineId != null) {
            return opportunityRepository.findByPipelineId(pipelineId, pageable);
        }
        if (status != null) {
            return opportunityRepository.findByStatus(status, pageable);
        }
        return opportunityRepository.findAll(pageable);
    }

    public CrmOpportunity require(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Oportunidad no encontrada"));
    }

    @Transactional
    public CrmOpportunity create(OpportunityCreateRequest request) {
        partnerService.require(request.partnerId());
        CrmPipeline pipeline = pipelineService.require(request.pipelineId());
        CrmPipelineStage stage = request.stageId() == null
                ? pipeline.entryStage()
                : pipeline.requireActiveStage(request.stageId());
        Money expected = resolveExpected(request.expectedAmount(), request.currency());

        CrmOpportunity opportunity = CrmOpportunity.open(
                request.name(),
                request.partnerId(),
                request.leadId(),
                pipeline.getId(),
                stage,
                expected,
                request.notes(),
                timeProvider.now()
        );
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public CrmOpportunity update(Long id, OpportunityUpdateRequest request) {
        CrmOpportunity opportunity = require(id);
        opportunity.rename(request.name(), request.notes());
        if (request.expectedAmount() != null || (request.currency() != null && !request.currency().isBlank())) {
            BigDecimal amount = request.expectedAmount() == null
                    ? opportunity.getExpectedAmount()
                    : request.expectedAmount();
            String currency = request.currency() == null || request.currency().isBlank()
                    ? opportunity.getCurrency()
                    : request.currency();
            opportunity.changeExpectedRevenue(resolveExpected(amount, currency));
        }
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public CrmOpportunity moveStage(Long id, OpportunityMoveStageRequest request) {
        CrmOpportunity opportunity = require(id);
        CrmPipeline pipeline = pipelineService.require(opportunity.getPipelineId());
        CrmPipelineStage stage = pipeline.requireActiveStage(request.stageId());
        opportunity.moveToStage(stage, timeProvider.now());
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public CrmOpportunity win(Long id) {
        CrmOpportunity opportunity = require(id);
        CrmPipeline pipeline = pipelineService.require(opportunity.getPipelineId());
        opportunity.win(pipeline.findWonStage(), timeProvider.now());
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public CrmOpportunity lose(Long id, OpportunityLoseRequest request) {
        CrmOpportunity opportunity = require(id);
        CrmPipeline pipeline = pipelineService.require(opportunity.getPipelineId());
        opportunity.lose(pipeline.findLostStage(), request == null ? null : request.reason(), timeProvider.now());
        return opportunityRepository.save(opportunity);
    }

    @Transactional
    public CrmOpportunity cancel(Long id) {
        CrmOpportunity opportunity = require(id);
        opportunity.cancel(timeProvider.now());
        return opportunityRepository.save(opportunity);
    }

    public static OpportunityStatus requireStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return OpportunityStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de oportunidad no soportado: " + raw);
        }
    }

    private Money resolveExpected(BigDecimal amount, String currency) {
        Currency resolved = currency == null || currency.isBlank()
                ? CrmPolicy.DEFAULT_CURRENCY
                : Currency.of(currency);
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return Money.of(value, resolved.code());
    }
}
