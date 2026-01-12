package com.nomos.store.service.controller;

import com.nomos.store.service.model.AccountsReceivable;
import com.nomos.store.service.model.Installment;
import com.nomos.store.service.repository.AccountsReceivableRepository;
import com.nomos.store.service.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/accounts-receivable")
@RequiredArgsConstructor
public class AccountsReceivableController {

    private final AccountsReceivableRepository arRepository;
    private final InstallmentRepository installmentRepository;

    /**
     * Obtener todas las cuentas por cobrar.
     * Útil para el Dashboard de Finanzas.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<AccountsReceivable>> getAll() {
        return ResponseEntity.ok(arRepository.findAll());
    }

    /**
     * Buscar la cuenta por cobrar específica de una Venta.
     * Frontend: Cuando entres al detalle de una venta, llama a esto para mostrar el cronograma.
     */
    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<AccountsReceivable> getBySaleId(@PathVariable Long saleId) {
        return arRepository.findBySaleId(saleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener solo las cuotas (cronograma) de una cuenta por cobrar.
     */
    @GetMapping("/{id}/installments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<Installment>> getInstallments(@PathVariable Long id) {
        if (!arRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(installmentRepository.findByAccountsReceivableId(id));
    }


    /**
     * Obtener una cuenta por cobrar por su ID directo.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<AccountsReceivable> getById(@PathVariable Long id) {
        return arRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}