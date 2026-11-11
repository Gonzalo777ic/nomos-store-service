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

/**
 * Cobros/pagos y conciliación hacia facturas (#76).
 * Alta/publicación de facturas: {@link InvoiceDocumentController}.
 */
@RestController
@RequestMapping("/api/accounting/payments")
public class PaymentReconciliationController {

    private final PaymentReconciliationService reconciliationService;

    public PaymentReconciliationController(PaymentReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping
    public List<PaymentResponse> list(@RequestParam Long companyId) {
        return reconciliationService.listPayments(companyId).stream().map(PaymentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return PaymentResponse.from(reconciliationService.requirePayment(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@RequestBody PaymentCreateRequest request) {
        return PaymentResponse.from(reconciliationService.createPayment(request));
    }

    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable Long id) {
        return PaymentResponse.from(reconciliationService.confirmPayment(id));
    }

    @PostMapping("/{id}/reconcile")
    public PaymentResponse reconcile(@PathVariable Long id, @RequestBody PaymentReconcileRequest request) {
        return PaymentResponse.from(reconciliationService.reconcile(id, request));
    }
}
