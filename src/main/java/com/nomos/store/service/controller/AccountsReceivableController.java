package com.nomos.store.service.controller;

import com.nomos.store.service.repository.AccountsReceivableRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.receivable.AccountsReceivableResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/store/accounts-receivable")
public class AccountsReceivableController {

    private final AccountsReceivableRepository arRepository;

    public AccountsReceivableController(AccountsReceivableRepository arRepository) {
        this.arRepository = arRepository;
    }

    @GetMapping
    public List<AccountsReceivableResponse> getAll() {
        return arRepository.findAll().stream().map(AccountsReceivableResponse::from).toList();
    }

    @GetMapping("/sale/{saleId}")
    public AccountsReceivableResponse getBySaleId(@PathVariable Long saleId) {
        return arRepository.findBySaleId(saleId)
                .map(AccountsReceivableResponse::from)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "CxC no encontrada"));
    }

    @GetMapping("/{id}/installments")
    public List<AccountsReceivableResponse.InstallmentResponse> getInstallments(@PathVariable Long id) {
        return AccountsReceivableResponse.from(require(id)).installments();
    }

    @GetMapping("/{id}")
    public AccountsReceivableResponse getById(@PathVariable Long id) {
        return AccountsReceivableResponse.from(require(id));
    }

    private com.nomos.store.service.model.AccountsReceivable require(Long id) {
        return arRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "CxC no encontrada"));
    }
}
