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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/accounting/accounts")
public class ChartAccountController {

    private final ChartAccountService chartAccountService;

    public ChartAccountController(ChartAccountService chartAccountService) {
        this.chartAccountService = chartAccountService;
    }

    @GetMapping("/classes")
    public List<CatalogItem> classes() {
        return Arrays.stream(AccountClass.values())
                .map(accountClass -> new CatalogItem(accountClass.name(), accountClass.label()))
                .toList();
    }

    @GetMapping
    public PageResponse<ChartAccountResponse> list(
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean rootsOnly,
            @PageableDefault(size = AccountingPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        var page = rootsOnly
                ? chartAccountService.listRoots(companyId, pageable)
                : chartAccountService.listByCompany(companyId, pageable);
        return PageResponse.from(page.map(ChartAccountResponse::from));
    }

    @GetMapping("/{id}")
    public ChartAccountResponse get(@PathVariable Long id) {
        return ChartAccountResponse.from(chartAccountService.require(id));
    }

    @GetMapping("/{id}/children")
    public List<ChartAccountResponse> children(@PathVariable Long id, @RequestParam Long companyId) {
        return chartAccountService.listChildren(companyId, id).stream()
                .map(ChartAccountResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChartAccountResponse create(@RequestBody ChartAccountCreateRequest request) {
        return ChartAccountResponse.from(chartAccountService.create(request));
    }

    @PutMapping("/{id}")
    public ChartAccountResponse update(@PathVariable Long id, @RequestBody ChartAccountUpdateRequest request) {
        return ChartAccountResponse.from(chartAccountService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        chartAccountService.retire(id);
        return ResponseEntity.noContent().build();
    }

    public record CatalogItem(String key, String label) {
    }
}
