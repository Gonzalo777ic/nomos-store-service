package com.valeerp.accounting;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounting/journal-entries")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping
    public PageResponse<JournalEntryResponse> list(
            @RequestParam Long companyId,
            @PageableDefault(size = AccountingPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(journalEntryService.list(companyId, pageable).map(JournalEntryResponse::from));
    }

    @GetMapping("/{id}")
    public JournalEntryResponse get(@PathVariable Long id) {
        return JournalEntryResponse.from(journalEntryService.require(id));
    }

    @GetMapping("/by-reference")
    public List<JournalEntryResponse> byReference(@RequestParam String reference) {
        return journalEntryService.byReference(reference).stream().map(JournalEntryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryResponse create(@RequestBody JournalEntryDraftRequest request) {
        return JournalEntryResponse.from(journalEntryService.createDraft(request));
    }

    @PostMapping("/{id}/post")
    public JournalEntryResponse post(@PathVariable Long id) {
        return JournalEntryResponse.from(journalEntryService.post(id));
    }

    @PostMapping("/{id}/void")
    public JournalEntryResponse voidEntry(@PathVariable Long id,
                                          @RequestBody(required = false) VoidEntryRequest request) {
        return JournalEntryResponse.from(journalEntryService.voidPosted(
                id, request == null ? null : request.reversalDate()));
    }
}
