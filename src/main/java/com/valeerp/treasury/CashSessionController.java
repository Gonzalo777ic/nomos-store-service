package com.valeerp.treasury;

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

@RestController
@RequestMapping("/api/treasury/cash-sessions")
public class CashSessionController {

    private final CashSessionService cashSessionService;

    public CashSessionController(CashSessionService cashSessionService) {
        this.cashSessionService = cashSessionService;
    }

    @GetMapping
    public PageResponse<CashSessionResponse> list(
            @RequestParam Long cashRegisterId,
            @PageableDefault(size = TreasuryPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(
                cashSessionService.listByRegister(cashRegisterId, pageable).map(CashSessionResponse::from));
    }

    @GetMapping("/{id}")
    public CashSessionResponse get(@PathVariable Long id) {
        return CashSessionResponse.from(cashSessionService.require(id));
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public CashSessionResponse open(@RequestBody CashSessionOpenRequest request) {
        return CashSessionResponse.from(cashSessionService.open(request));
    }

    @PostMapping("/{id}/close")
    public CashSessionResponse close(@PathVariable Long id, @RequestBody CashSessionCloseRequest request) {
        return CashSessionResponse.from(cashSessionService.close(id, request));
    }
}
