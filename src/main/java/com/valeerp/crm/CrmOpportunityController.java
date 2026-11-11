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

@RestController
@RequestMapping("/api/crm/opportunities")
public class CrmOpportunityController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CrmOpportunityService opportunityService;

    public CrmOpportunityController(CrmOpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public PageResponse<OpportunityResponse> list(
            @RequestParam(required = false) Long pipelineId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Long partnerId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        OpportunityStatus opportunityStatus = CrmOpportunityService.requireStatus(status);
        return PageResponse.from(opportunityService
                .list(pipelineId, stageId, partnerId, opportunityStatus, pageable)
                .map(OpportunityResponse::from));
    }

    @GetMapping("/{id}")
    public OpportunityResponse get(@PathVariable Long id) {
        return OpportunityResponse.from(opportunityService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(@RequestBody OpportunityCreateRequest request) {
        return OpportunityResponse.from(opportunityService.create(request));
    }

    @PutMapping("/{id}")
    public OpportunityResponse update(@PathVariable Long id, @RequestBody OpportunityUpdateRequest request) {
        return OpportunityResponse.from(opportunityService.update(id, request));
    }

    @PostMapping("/{id}/move-stage")
    public OpportunityResponse moveStage(@PathVariable Long id, @RequestBody OpportunityMoveStageRequest request) {
        return OpportunityResponse.from(opportunityService.moveStage(id, request));
    }

    @PostMapping("/{id}/win")
    public OpportunityResponse win(@PathVariable Long id) {
        return OpportunityResponse.from(opportunityService.win(id));
    }

    @PostMapping("/{id}/lose")
    public OpportunityResponse lose(@PathVariable Long id, @RequestBody OpportunityLoseRequest request) {
        return OpportunityResponse.from(opportunityService.lose(id, request));
    }

    @PostMapping("/{id}/cancel")
    public OpportunityResponse cancel(@PathVariable Long id) {
        return OpportunityResponse.from(opportunityService.cancel(id));
    }

    @GetMapping("/statuses")
    public List<CatalogItem> statuses() {
        return Arrays.stream(OpportunityStatus.values())
                .map(status -> new CatalogItem(status.name(), status.name()))
                .toList();
    }

    public record CatalogItem(String key, String label) {
    }
}
