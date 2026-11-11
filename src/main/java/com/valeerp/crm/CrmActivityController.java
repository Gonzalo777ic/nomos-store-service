package com.valeerp.crm;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/crm")
public class CrmActivityController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CrmActivityService activityService;

    public CrmActivityController(CrmActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/activity-types")
    public List<CatalogItem> activityTypes() {
        return Arrays.stream(ActivityType.values())
                .map(type -> new CatalogItem(type.name(), type.name()))
                .toList();
    }

    @GetMapping("/activities")
    public PageResponse<ActivityResponse> list(
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        ActivityStatus parsed = parseStatus(status);
        return PageResponse.from(activityService.list(opportunityId, parsed, pageable).map(ActivityResponse::from));
    }

    @GetMapping("/opportunities/{opportunityId}/activities")
    public List<ActivityResponse> listForOpportunity(@PathVariable Long opportunityId) {
        return activityService.listForOpportunity(opportunityId).stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @PostMapping("/opportunities/{opportunityId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse schedule(@PathVariable Long opportunityId, @RequestBody ActivityCreateRequest request) {
        return ActivityResponse.from(activityService.schedule(opportunityId, request));
    }

    @GetMapping("/activities/{id}")
    public ActivityResponse get(@PathVariable Long id) {
        return ActivityResponse.from(activityService.require(id));
    }

    @PutMapping("/activities/{id}")
    public ActivityResponse reschedule(@PathVariable Long id, @RequestBody ActivityUpdateRequest request) {
        return ActivityResponse.from(activityService.reschedule(id, request));
    }

    @PostMapping("/activities/{id}/complete")
    public ActivityResponse complete(@PathVariable Long id, @RequestBody(required = false) ActivityCompleteRequest request) {
        return ActivityResponse.from(activityService.complete(id, request));
    }

    @PostMapping("/activities/{id}/cancel")
    public ActivityResponse cancel(@PathVariable Long id, @RequestBody ActivityCancelRequest request) {
        return ActivityResponse.from(activityService.cancel(id, request));
    }

    private static ActivityStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ActivityStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new com.valeerp.kernel.DomainException("Estado de actividad no soportado: " + raw);
        }
    }

    public record CatalogItem(String key, String label) {
    }
}
