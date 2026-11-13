package com.valeerp.store.service.controller;

import com.valeerp.treasury.PaymentMethodCreateRequest;
import com.valeerp.treasury.PaymentMethodResponse;
import com.valeerp.treasury.PaymentMethodService;
import com.valeerp.treasury.PaymentMethodUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Ruta legacy {@code /api/store/payment-methods}. Delegada al caso de uso de tesorería (DTOs out).
 */
@RestController
@RequestMapping("/api/store/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodConfigController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public List<PaymentMethodResponse> getAll() {
        return paymentMethodService.list().stream().map(PaymentMethodResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaymentMethodResponse> create(@RequestBody Map<String, Object> body) {
        PaymentMethodCreateRequest request = new PaymentMethodCreateRequest(
                stringOrNull(body.get("code"), stringOrNull(body.get("name"), null)),
                stringOrNull(body.get("name"), null),
                stringOrNull(body.get("kind"), stringOrNull(body.get("type"), null)),
                body.get("requiresReference") instanceof Boolean flag && flag
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentMethodResponse.from(paymentMethodService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaymentMethodResponse> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PaymentMethodUpdateRequest request = new PaymentMethodUpdateRequest(
                stringOrNull(body.get("code"), stringOrNull(body.get("name"), null)),
                stringOrNull(body.get("name"), null),
                stringOrNull(body.get("kind"), stringOrNull(body.get("type"), null)),
                body.get("requiresReference") instanceof Boolean flag && flag
        );
        return ResponseEntity.ok(PaymentMethodResponse.from(paymentMethodService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.retire(id);
        return ResponseEntity.noContent().build();
    }

    private static String stringOrNull(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }
}
