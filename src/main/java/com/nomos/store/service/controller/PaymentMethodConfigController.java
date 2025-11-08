package com.nomos.store.service.controller;

import com.nomos.store.service.model.PaymentMethodConfig;
import com.nomos.store.service.repository.PaymentMethodConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/store/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodConfigController {

    private final PaymentMethodConfigRepository repository;

    /** 🔑 GET /api/store/payment-methods - Obtener todos los métodos */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public List<PaymentMethodConfig> getAll() {
        return repository.findAll();
    }

    /** 🔑 POST /api/store/payment-methods - Crear nuevo método (Solo Admin) */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaymentMethodConfig> create(@RequestBody PaymentMethodConfig config) {
        try {
            PaymentMethodConfig savedConfig = repository.save(config);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedConfig);
        } catch (DataIntegrityViolationException e) {
            // Manejo de nombre duplicado
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /** 🔑 PUT /api/store/payment-methods/{id} - Actualizar método (Solo Admin) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PaymentMethodConfig> update(@PathVariable Long id, @RequestBody PaymentMethodConfig updatedConfig) {
        Optional<PaymentMethodConfig> existingConfigOpt = repository.findById(id);

        if (existingConfigOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PaymentMethodConfig existingConfig = existingConfigOpt.get();
        existingConfig.setName(updatedConfig.getName());
        existingConfig.setType(updatedConfig.getType());

        try {
            PaymentMethodConfig savedConfig = repository.save(existingConfig);
            return ResponseEntity.ok(savedConfig);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /** 🔑 DELETE /api/store/payment-methods/{id} - Eliminar método (Solo Admin) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}