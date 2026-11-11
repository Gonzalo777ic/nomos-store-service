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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/treasury/cash-bank-transfers")
public class CashBankTransferController {

    private final CashBankTransferService cashBankTransferService;

    public CashBankTransferController(CashBankTransferService cashBankTransferService) {
        this.cashBankTransferService = cashBankTransferService;
    }

    @GetMapping("/directions")
    public List<CatalogItem> directions() {
        return Arrays.stream(TransferDirection.values())
                .map(direction -> new CatalogItem(direction.name(), direction.label()))
                .toList();
    }

    @GetMapping
    public PageResponse<CashBankTransferResponse> list(
            @RequestParam Long companyId,
            @PageableDefault(size = TreasuryPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(
                cashBankTransferService.list(companyId, pageable).map(CashBankTransferResponse::from));
    }

    @GetMapping("/{id}")
    public CashBankTransferResponse get(@PathVariable Long id) {
        return CashBankTransferResponse.from(cashBankTransferService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashBankTransferResponse create(@RequestBody CashBankTransferCreateRequest request) {
        return CashBankTransferResponse.from(cashBankTransferService.create(request));
    }

    @PostMapping("/{id}/post")
    public CashBankTransferResponse post(@PathVariable Long id) {
        return CashBankTransferResponse.from(cashBankTransferService.post(id));
    }

    @PostMapping("/{id}/void")
    public CashBankTransferResponse voidTransfer(
            @PathVariable Long id,
            @RequestBody(required = false) CashMovementVoidRequest request) {
        String reason = request == null ? null : request.reason();
        return CashBankTransferResponse.from(cashBankTransferService.voidTransfer(id, reason));
    }

    public record CatalogItem(String code, String label) {
    }
}
