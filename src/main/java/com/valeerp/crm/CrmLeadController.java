package com.valeerp.crm;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/crm/leads")
public class CrmLeadController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CrmLeadService leadService;

    public CrmLeadController(CrmLeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public PageResponse<LeadResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long partnerId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        LeadStatus leadStatus = CrmLeadService.requireStatus(status);
        return PageResponse.from(leadService.list(leadStatus, partnerId, pageable).map(LeadResponse::from));
    }

    @GetMapping("/{id}")
    public LeadResponse get(@PathVariable Long id) {
        return LeadResponse.from(leadService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse create(@RequestBody LeadCreateRequest request) {
        return LeadResponse.from(leadService.create(request));
    }

    @PutMapping("/{id}")
    public LeadResponse update(@PathVariable Long id, @RequestBody LeadUpdateRequest request) {
        return LeadResponse.from(leadService.update(id, request));
    }

    @PostMapping("/{id}/qualify")
    public LeadResponse qualify(@PathVariable Long id) {
        return LeadResponse.from(leadService.qualify(id));
    }

    @PostMapping("/{id}/disqualify")
    public LeadResponse disqualify(@PathVariable Long id, @RequestBody LeadDisqualifyRequest request) {
        return LeadResponse.from(leadService.disqualify(id, request));
    }

    @PostMapping("/{id}/convert")
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse convert(@PathVariable Long id, @RequestBody LeadConvertRequest request) {
        return OpportunityResponse.from(leadService.convert(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        leadService.retire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statuses")
    public List<CatalogItem> statuses() {
        return Arrays.stream(LeadStatus.values())
                .map(status -> new CatalogItem(status.name(), status.name()))
                .toList();
    }

    public record CatalogItem(String key, String label) {
    }
}
