package com.nomos.store.service.controller;

import com.nomos.store.service.model.AccountingJournalEntry;
import com.nomos.store.service.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting/entries")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    /**
     * GET /api/accounting/entries
     * Lista el historial de todos los movimientos contables (El Libro Diario).
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT')") // Ajusta los roles según tu sistema
    public ResponseEntity<List<AccountingJournalEntry>> getAllEntries() {
        return ResponseEntity.ok(accountingService.getAllEntries());
    }

    /**
     * GET /api/accounting/entries/{id}
     * Ver el detalle de un asiento específico (sus líneas de debe/haber).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ACCOUNTANT')")
    public ResponseEntity<AccountingJournalEntry> getEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(accountingService.getEntryById(id));
    }

    /**
     * GET /api/accounting/entries/reference/{ref}
     * Buscar asientos por documento de referencia (ej: SALE-123).
     */
    @GetMapping("/reference/{ref}")
    public ResponseEntity<List<AccountingJournalEntry>> getEntriesByReference(@PathVariable String ref) {
        return ResponseEntity.ok(accountingService.getEntriesByReference(ref));
    }
}