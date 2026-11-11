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

@RestController
@RequestMapping("/api/accounting/invoices")
public class InvoiceDocumentController {

    private final InvoiceDocumentService invoiceDocumentService;

    public InvoiceDocumentController(InvoiceDocumentService invoiceDocumentService) {
        this.invoiceDocumentService = invoiceDocumentService;
    }

    @GetMapping
    public PageResponse<InvoiceDocumentResponse> list(
            @RequestParam Long companyId,
            @RequestParam(required = false) String side,
            @PageableDefault(size = AccountingPolicy.DEFAULT_PAGE_SIZE) Pageable pageable) {
        InvoiceSide invoiceSide = side == null || side.isBlank() ? null : InvoiceSide.require(side);
        return PageResponse.from(
                invoiceDocumentService.list(companyId, invoiceSide, pageable).map(InvoiceDocumentResponse::from));
    }

    @GetMapping("/{id}")
    public InvoiceDocumentResponse get(@PathVariable Long id) {
        return InvoiceDocumentResponse.from(invoiceDocumentService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDocumentResponse create(@RequestBody InvoiceDocumentCreateRequest request) {
        return InvoiceDocumentResponse.from(invoiceDocumentService.createDraft(request));
    }

    @PostMapping("/{id}/post")
    public InvoiceDocumentResponse post(@PathVariable Long id) {
        return InvoiceDocumentResponse.from(invoiceDocumentService.post(id));
    }

    @PostMapping("/{id}/void")
    public InvoiceDocumentResponse voidByReversal(@PathVariable Long id) {
        return InvoiceDocumentResponse.from(invoiceDocumentService.voidByReversal(id));
    }
}
