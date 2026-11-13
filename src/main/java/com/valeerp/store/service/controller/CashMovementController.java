package com.valeerp.store.service.controller;

import com.valeerp.store.service.model.CashMovement;
import com.valeerp.store.service.model.dto.CashMovementDTO;
import com.valeerp.store.service.service.CashMovementService;
import com.valeerp.store.service.service.CashMovementService.ManualMovementPayload;
import com.valeerp.treasury.CashMovementResponse;
import com.valeerp.treasury.CashMovementVoidRequest;
import com.valeerp.treasury.TreasuryCashMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/store/cash-movements")
@RequiredArgsConstructor
public class CashMovementController {

    private final CashMovementService service;
    private final TreasuryCashMovementService treasuryCashMovementService;

    @GetMapping
    public ResponseEntity<List<CashMovementDTO>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<CashMovementDTO> dtos = service.getMovementsByFilter(startDate, endDate).stream()
                .map(CashMovementDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CashMovementDTO> create(@RequestBody ManualMovementPayload payload) {
        Long userId = 1L;
        CashMovement created = service.createManualMovement(payload, userId);
        return new ResponseEntity<>(CashMovementDTO.fromEntity(created), HttpStatus.CREATED);
    }

    @GetMapping("/daily")
    public ResponseEntity<List<CashMovementDTO>> getDaily() {
        List<CashMovementDTO> dtos = service.getDailyMovements().stream()
                .map(CashMovementDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<CashMovementResponse> voidMovement(
            @PathVariable Long id,
            @RequestBody(required = false) CashMovementVoidRequest request) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(CashMovementResponse.from(treasuryCashMovementService.voidMovement(id, reason)));
    }
}
