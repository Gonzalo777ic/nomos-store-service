package com.nomos.store.service.controller;

import com.nomos.store.service.model.Sale;
import com.nomos.store.service.service.SaleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/store/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;


    @Data
    public static class SaleRequestDetail {
        private Long productId;
        private Double unitPrice;
        private Integer quantity;
        private Double subtotal;
        private Long taxRateId;
        private Long promotionId;
    }

    @Data
    public static class SaleCreationRequest {
        private Long clientId;
        private LocalDateTime saleDate;
        private LocalDate creditStartDate;
        private String type;
        private String paymentCondition;
        private Integer creditDays;
        private Long sellerId;
        private List<SaleRequestDetail> details;
        private Integer numberOfInstallments;
    }

    @Data
    public static class ReferenceDTO {
        private final String key;
        private final String description;
    }


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_VENDOR')")
    public ResponseEntity<List<Sale>> getAllSales() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER', 'ROLE_VENDOR')")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        return saleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/types")
    public ResponseEntity<List<ReferenceDTO>> getSaleTypes() {
        return ResponseEntity.ok(saleService.getSaleTypes());
    }

    @GetMapping("/payment-conditions")
    public ResponseEntity<List<ReferenceDTO>> getPaymentConditions() {
        return ResponseEntity.ok(saleService.getPaymentConditions());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> createSale(@RequestBody SaleCreationRequest request) {
        try {
            Sale createdSale = saleService.createSale(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSale);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la venta: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> cancelSale(@PathVariable Long id) {
        try {
            saleService.cancelSale(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}