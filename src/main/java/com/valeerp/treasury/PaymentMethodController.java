package com.valeerp.treasury;

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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/treasury/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping("/kinds")
    public List<CatalogItem> kinds() {
        return Arrays.stream(PaymentMethodKind.values())
                .map(kind -> new CatalogItem(kind.name(), kind.label()))
                .toList();
    }

    @GetMapping
    public List<PaymentMethodResponse> list() {
        return paymentMethodService.list().stream().map(PaymentMethodResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PaymentMethodResponse get(@PathVariable Long id) {
        return PaymentMethodResponse.from(paymentMethodService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentMethodResponse create(@RequestBody PaymentMethodCreateRequest request) {
        return PaymentMethodResponse.from(paymentMethodService.create(request));
    }

    @PutMapping("/{id}")
    public PaymentMethodResponse update(@PathVariable Long id, @RequestBody PaymentMethodUpdateRequest request) {
        return PaymentMethodResponse.from(paymentMethodService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        paymentMethodService.retire(id);
        return ResponseEntity.noContent().build();
    }

    public record CatalogItem(String code, String label) {
    }
}
