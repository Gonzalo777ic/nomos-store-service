package com.valeerp.crm;

import com.valeerp.kernel.Actors;
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
public class CrmLeadService {

    private final CrmLeadRepository leadRepository;
    private final CrmOpportunityRepository opportunityRepository;
    private final CrmPipelineService pipelineService;
    private final PartnerService partnerService;
    private final TimeProvider timeProvider;

    public CrmLeadService(CrmLeadRepository leadRepository,
                          CrmOpportunityRepository opportunityRepository,
                          CrmPipelineService pipelineService,
                          PartnerService partnerService,
                          TimeProvider timeProvider) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.pipelineService = pipelineService;
        this.partnerService = partnerService;
        this.timeProvider = timeProvider;
    }

    public Page<CrmLead> list(LeadStatus status, Long partnerId, Pageable pageable) {
        if (partnerId != null) {
            return leadRepository.findByPartnerId(partnerId, pageable);
        }
        if (status != null) {
            return leadRepository.findByStatus(status, pageable);
        }
        return leadRepository.findAll(pageable);
    }

    public CrmLead require(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Lead no encontrado"));
    }

    @Transactional
    public CrmLead create(LeadCreateRequest request) {
        ensurePartner(request.partnerId());
        CrmLead lead = CrmLead.capture(
                request.title(),
                request.contactName(),
                request.email(),
                request.phone(),
                request.partnerId(),
                request.source(),
                request.notes(),
                timeProvider.now()
        );
        return leadRepository.save(lead);
    }

    @Transactional
    public CrmLead update(Long id, LeadUpdateRequest request) {
        CrmLead lead = require(id);
        ensurePartner(request.partnerId());
        lead.updateProfile(
                request.title(),
                request.contactName(),
                request.email(),
                request.phone(),
                request.partnerId(),
                request.source(),
                request.notes()
        );
        return leadRepository.save(lead);
    }

    @Transactional
    public CrmLead qualify(Long id) {
        CrmLead lead = require(id);
        lead.qualify();
        return leadRepository.save(lead);
    }

    @Transactional
    public CrmLead disqualify(Long id, LeadDisqualifyRequest request) {
        CrmLead lead = require(id);
        lead.disqualify(request == null ? null : request.reason());
        return leadRepository.save(lead);
    }

    @Transactional
    public CrmOpportunity convert(Long id, LeadConvertRequest request) {
        CrmLead lead = require(id);
        if (lead.getStatus() == LeadStatus.DISQUALIFIED) {
            throw new DomainException("Un lead descalificado no se puede convertir");
        }
        Long partnerId = lead.getPartnerId();
        if (partnerId == null) {
            throw new DomainException("El lead exige partnerId antes de convertirse");
        }
        ensurePartner(partnerId);

        CrmPipeline pipeline = pipelineService.require(request.pipelineId());
        CrmPipelineStage stage = request.stageId() == null
                ? pipeline.entryStage()
                : pipeline.requireActiveStage(request.stageId());

        String opportunityName = request.opportunityName() == null || request.opportunityName().isBlank()
                ? lead.getTitle()
                : request.opportunityName();
        Money expected = resolveExpected(request.expectedAmount(), request.currency());

        CrmOpportunity opportunity = CrmOpportunity.open(
                opportunityName,
                partnerId,
                lead.getId(),
                pipeline.getId(),
                stage,
                expected,
                request.notes() == null ? lead.getNotes() : request.notes(),
                timeProvider.now()
        );
        opportunity = opportunityRepository.save(opportunity);
        lead.markConverted(opportunity.getId());
        leadRepository.save(lead);
        return opportunity;
    }

    @Transactional
    public void retire(Long id) {
        CrmLead lead = require(id);
        lead.archive(Actors.currentActor(), timeProvider.now());
        leadRepository.save(lead);
    }

    public static LeadStatus requireStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LeadStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de lead no soportado: " + raw);
        }
    }

    private void ensurePartner(Long partnerId) {
        if (partnerId != null) {
            partnerService.require(partnerId);
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
