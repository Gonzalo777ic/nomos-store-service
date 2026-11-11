package com.valeerp.helpdesk;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/helpdesk/slas")
public class HelpdeskSlaController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final HelpdeskSlaService slaService;

    public HelpdeskSlaController(HelpdeskSlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping
    public PageResponse<SlaResponse> list(@PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(slaService.list(pageable).map(SlaResponse::from));
    }

    @GetMapping("/{id}")
    public SlaResponse get(@PathVariable Long id) {
        return SlaResponse.from(slaService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlaResponse create(@RequestBody SlaCreateRequest request) {
        return SlaResponse.from(slaService.create(request));
    }

    @PutMapping("/{id}")
    public SlaResponse update(@PathVariable Long id, @RequestBody SlaUpdateRequest request) {
        return SlaResponse.from(slaService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        slaService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
