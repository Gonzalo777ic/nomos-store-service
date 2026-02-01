package com.nomos.store.service.controller;

import com.nomos.store.service.model.CashMovement;
import com.nomos.store.service.service.CashMovementService;
import com.nomos.store.service.service.CashMovementService.ManualMovementPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/store/cash-movements")
@RequiredArgsConstructor
public class CashMovementController {

    private final CashMovementService service;

    /**
     * GET /api/store/cash-movements
     * Soporta filtros opcionales: ?startDate=2026-02-01&endDate=2026-02-01
     */
    @GetMapping
    public ResponseEntity<List<CashMovement>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(service.getMovementsByFilter(startDate, endDate));
    }

    /**
     * POST /api/store/cash-movements
     * Crea un movimiento manual (Gasto o Ingreso Extra).
     */
    @PostMapping
    public ResponseEntity<CashMovement> create(@RequestBody ManualMovementPayload payload) {
        // Asumimos userId = 1L temporalmente (idealmente lo sacas del token JWT)
        Long userId = 1L;

        CashMovement created = service.createManualMovement(payload, userId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


}