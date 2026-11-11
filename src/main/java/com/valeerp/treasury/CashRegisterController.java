package com.valeerp.treasury;

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
@RequestMapping("/api/treasury/cash-registers")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @GetMapping
    public PageResponse<CashRegisterResponse> list(
            @RequestParam Long companyId,
            @PageableDefault(size = TreasuryPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(cashRegisterService.list(companyId, pageable).map(CashRegisterResponse::from));
    }

    @GetMapping("/{id}")
    public CashRegisterResponse get(@PathVariable Long id) {
        return CashRegisterResponse.from(cashRegisterService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashRegisterResponse create(@RequestBody CashRegisterCreateRequest request) {
        return CashRegisterResponse.from(cashRegisterService.create(request));
    }

    @PutMapping("/{id}")
    public CashRegisterResponse update(@PathVariable Long id, @RequestBody CashRegisterUpdateRequest request) {
        return CashRegisterResponse.from(cashRegisterService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        cashRegisterService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
