package com.valeerp.partner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partners/payment-terms")
public class PaymentTermController {

    private final PaymentTermService paymentTermService;

    public PaymentTermController(PaymentTermService paymentTermService) {
        this.paymentTermService = paymentTermService;
    }

    @GetMapping
    public List<PaymentTermResponse> list() {
        return paymentTermService.list().stream().map(PaymentTermResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PaymentTermResponse get(@PathVariable Long id) {
        return PaymentTermResponse.from(paymentTermService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentTermResponse create(@RequestBody PaymentTermRequest request) {
        return PaymentTermResponse.from(paymentTermService.create(request));
    }

    @PutMapping("/{id}")
    public PaymentTermResponse update(@PathVariable Long id, @RequestBody PaymentTermRequest request) {
        return PaymentTermResponse.from(paymentTermService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        paymentTermService.retire(id);
        return ResponseEntity.noContent().build();
    }
}
