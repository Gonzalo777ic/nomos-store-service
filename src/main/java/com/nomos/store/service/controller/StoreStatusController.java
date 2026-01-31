package com.nomos.store.service.controller;

import com.nomos.store.service.model.dto.StoreStatusDTO;
import com.nomos.store.service.service.StoreStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/status")
@RequiredArgsConstructor
public class StoreStatusController {

    private final StoreStatusService statusService;

    /**
     * Endpoint PRINCIPAL para saber si la tienda está atendiendo.
     * No requiere autenticación (generalmente) si es para mostrar en el e-commerce,
     * pero para ERP probablemente sí requiera token.
     */
    @GetMapping("/current")
    public ResponseEntity<StoreStatusDTO> getCurrentStatus() {
        return ResponseEntity.ok(statusService.getCurrentStatus());
    }
}