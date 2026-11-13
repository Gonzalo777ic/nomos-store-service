package com.valeerp.store.service.controller;

import com.valeerp.store.service.model.SaleDetail;
import com.valeerp.store.service.repository.SaleDetailRepository;
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

    /**
     * GET /api/store/saledetails/sale/{saleId}
     * Spring Data JPA es inteligente: findBySaleId busca el campo "sale" y luego su "id" automáticamente.
     */
    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<SaleDetail>> getDetailsBySaleId(@PathVariable Long saleId) {
        List<SaleDetail> details = saleDetailRepository.findBySaleId(saleId);

        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(details);
    }

    /** * POST /api/store/saledetails
     * JSON esperado ahora debe ser:
     * {
     * "sale": { "id": 1 },
     * "productId": 10,
     * "quantity": 2,
     * ...
     * }
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> addSaleDetail(@RequestBody SaleDetail detail) {
        return ResponseEntity.badRequest().body("Las líneas se registran con la venta. El detalle no se altera por fuera del agregado");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> deleteSaleDetail(@PathVariable Long id) {
        return ResponseEntity.badRequest().body("Una línea de venta no se borra. Anule la venta o registre una devolución");
    }
}