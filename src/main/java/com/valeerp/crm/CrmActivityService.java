package com.valeerp.crm;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmActivityService {

    private final CrmActivityRepository activityRepository;
    private final CrmOpportunityService opportunityService;
    private final TimeProvider timeProvider;

    public CrmActivityService(CrmActivityRepository activityRepository,
                              CrmOpportunityService opportunityService,
                              TimeProvider timeProvider) {
        this.activityRepository = activityRepository;
        this.opportunityService = opportunityService;
        this.timeProvider = timeProvider;
    }

    public List<CrmActivity> listForOpportunity(Long opportunityId) {
        opportunityService.require(opportunityId);
        return activityRepository.findByOpportunityIdOrderByCreatedAtDesc(opportunityId);
    }

    public Page<CrmActivity> list(Long opportunityId, ActivityStatus status, Pageable pageable) {
        if (opportunityId != null && status != null) {
            opportunityService.require(opportunityId);
            return activityRepository.findByOpportunityIdAndStatus(opportunityId, status, pageable);
        }
        if (opportunityId != null) {
            opportunityService.require(opportunityId);
            return activityRepository.findByOpportunityId(opportunityId, pageable);
        }
        if (status != null) {
            return activityRepository.findByStatus(status, pageable);
        }
        return activityRepository.findAll(pageable);
    }

    public CrmActivity require(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Actividad no encontrada"));
    }

    @Transactional
    public CrmActivity schedule(Long opportunityId, ActivityCreateRequest request) {
        CrmOpportunity opportunity = opportunityService.require(opportunityId);
        if (!opportunity.getStatus().isOpen()) {
            throw new DomainException("No se pueden programar actividades en una oportunidad cerrada");
        }
        CrmActivity activity = CrmActivity.schedule(
                opportunity.getId(),
                ActivityType.require(request.type()),
                request.subject(),
                request.notes(),
                request.dueAt(),
                timeProvider.now()
        );
        return activityRepository.save(activity);
    }

    @Transactional
    public CrmActivity reschedule(Long id, ActivityUpdateRequest request) {
        CrmActivity activity = require(id);
        activity.reschedule(request.subject(), request.notes(), request.dueAt());
        return activityRepository.save(activity);
    }

    @Transactional
    public CrmActivity complete(Long id, ActivityCompleteRequest request) {
        CrmActivity activity = require(id);
        activity.complete(timeProvider.now(), request == null ? null : request.notes());
        return activityRepository.save(activity);
    }

    @Transactional
    public CrmActivity cancel(Long id, ActivityCancelRequest request) {
        CrmActivity activity = require(id);
        String reason = request == null ? null : request.reason();
        activity.cancel(timeProvider.now(), reason);
        return activityRepository.save(activity);
    }
}
