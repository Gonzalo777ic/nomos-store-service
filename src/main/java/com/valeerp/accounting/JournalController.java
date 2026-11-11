package com.valeerp.accounting;

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

@RestController
@RequestMapping("/api/accounting/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    public PageResponse<JournalResponse> list(
            @RequestParam Long companyId,
            @RequestParam(required = false) String kind,
            @PageableDefault(size = AccountingPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        JournalKind journalKind = kind == null || kind.isBlank() ? null : JournalKind.require(kind);
        return PageResponse.from(journalService.list(companyId, journalKind, pageable).map(JournalResponse::from));
    }

    @GetMapping("/{id}")
    public JournalResponse get(@PathVariable Long id) {
        return JournalResponse.from(journalService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalResponse create(@RequestBody JournalCreateRequest request) {
        return JournalResponse.from(journalService.create(request));
    }

    @PutMapping("/{id}")
    public JournalResponse update(@PathVariable Long id, @RequestBody JournalUpdateRequest request) {
        return JournalResponse.from(journalService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        journalService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
