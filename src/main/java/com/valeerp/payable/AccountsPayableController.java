package com.valeerp.payable;

import com.valeerp.accounting.AccountingPolicy;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounting/accounts-payable")
public class AccountsPayableController {

    private final AccountsPayableService accountsPayableService;

    public AccountsPayableController(AccountsPayableService accountsPayableService) {
        this.accountsPayableService = accountsPayableService;
    }

    @GetMapping
    public PageResponse<AccountsPayableResponse> list(
            @RequestParam Long companyId,
            @PageableDefault(size = AccountingPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(accountsPayableService.list(companyId, pageable).map(AccountsPayableResponse::from));
    }

    @GetMapping("/{id}")
    public AccountsPayableResponse get(@PathVariable Long id) {
        return AccountsPayableResponse.from(accountsPayableService.require(id));
    }

    @PostMapping("/from-invoice/{supplierInvoiceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountsPayableResponse openFromInvoice(@PathVariable Long supplierInvoiceId) {
        return AccountsPayableResponse.from(accountsPayableService.openFromSupplierInvoice(supplierInvoiceId));
    }

    @PostMapping("/{id}/payments")
    public AccountsPayableResponse pay(@PathVariable Long id, @RequestBody PaymentRequest request) {
        return AccountsPayableResponse.from(accountsPayableService.applyPayment(
                id, request.paymentDate(), request.amount(), request.referenceNumber()));
    }

    @PostMapping("/{id}/payments/{paymentId}/cancel")
    public AccountsPayableResponse cancelPayment(@PathVariable Long id, @PathVariable Long paymentId) {
        return AccountsPayableResponse.from(accountsPayableService.cancelPayment(id, paymentId));
    }

    @PostMapping("/{id}/void")
    public AccountsPayableResponse voidByReversal(@PathVariable Long id) {
        return AccountsPayableResponse.from(accountsPayableService.voidByReversal(id));
    }

    public record PaymentRequest(LocalDate paymentDate, BigDecimal amount, String referenceNumber) {
    }
}
