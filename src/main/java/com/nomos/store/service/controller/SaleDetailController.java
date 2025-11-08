package com.nomos.store.service.controller;

import com.nomos.store.service.model.SaleDetail;
import com.nomos.store.service.repository.SaleDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/saledetails")
@RequiredArgsConstructor
public class SaleDetailController {

    private final SaleDetailRepository saleDetailRepository;

    /** 🔑 GET /api/store/saledetails/sale/{saleId} - Obtener todos los detalles de una venta específica */
    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<SaleDetail>> getDetailsBySaleId(@PathVariable Long saleId) {
        List<SaleDetail> details = saleDetailRepository.findBySaleId(saleId);
        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(details);
    }

    /** 🔑 POST /api/store/saledetails - Agregar un nuevo ítem de detalle de venta */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<SaleDetail> addSaleDetail(@RequestBody SaleDetail detail) {
        // Validación básica
        if (detail.getSaleId() == null || detail.getProductId() == null || detail.getQuantity() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SaleDetail newDetail = saleDetailRepository.save(detail);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDetail);
    }

    /** 🔑 DELETE /api/store/saledetails/{id} - Eliminar un ítem de detalle */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Void> deleteSaleDetail(@PathVariable Long id) {
        if (saleDetailRepository.existsById(id)) {
            saleDetailRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}