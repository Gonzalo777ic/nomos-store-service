package com.valeerp.accounting;

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
@RequestMapping("/api/accounting/periods")
public class AccountingPeriodController {

    private final AccountingPeriodService periodService;

    public AccountingPeriodController(AccountingPeriodService periodService) {
        this.periodService = periodService;
    }

    @GetMapping
    public List<AccountingPeriodResponse> list(@RequestParam Long companyId) {
        return periodService.list(companyId).stream().map(AccountingPeriodResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AccountingPeriodResponse get(@PathVariable Long id) {
        return AccountingPeriodResponse.from(periodService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountingPeriodResponse open(@RequestBody AccountingPeriodCreateRequest request) {
        return AccountingPeriodResponse.from(periodService.open(request));
    }

    @PostMapping("/{id}/close")
    public AccountingPeriodResponse close(@PathVariable Long id) {
        return AccountingPeriodResponse.from(periodService.close(id));
    }
}
