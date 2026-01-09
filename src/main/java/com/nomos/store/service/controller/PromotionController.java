package com.nomos.store.service.controller;

import com.nomos.store.service.model.Promotion;
import com.nomos.store.service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/store/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionRepository promotionRepository;

    /**  GET /api/store/promotions - Obtener todas las promociones (Admin) */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Promotion>> getAll() {
        return ResponseEntity.ok(promotionRepository.findAll());
    }

    /** *  GET /api/store/promotions/active - Obtener promociones activas (Cualquier usuario para cálculos de venta)
     * La lógica de negocio está aquí.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<Promotion>> getActive() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);
        return ResponseEntity.ok(activePromotions);
    }

    /**  POST /api/store/promotions - Crear nueva promoción (Admin) */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<Promotion> create(@RequestBody Promotion promotion) {
        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            Promotion newPromotion = promotionRepository.save(promotion);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPromotion);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**  PUT /api/store/promotions/{id} - Actualizar promoción (Admin) */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<Promotion> update(@PathVariable Long id, @RequestBody Promotion updatedPromotion) {
        Optional<Promotion> existingPromotionOpt = promotionRepository.findById(id);

        if (existingPromotionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (updatedPromotion.getStartDate().isAfter(updatedPromotion.getEndDate())) {
            return ResponseEntity.badRequest().build();
        }

        Promotion existingPromotion = existingPromotionOpt.get();
        existingPromotion.setName(updatedPromotion.getName());
        existingPromotion.setType(updatedPromotion.getType());
        existingPromotion.setDiscountValue(updatedPromotion.getDiscountValue());
        existingPromotion.setStartDate(updatedPromotion.getStartDate());
        existingPromotion.setEndDate(updatedPromotion.getEndDate());
        existingPromotion.setIsActive(updatedPromotion.getIsActive());
        existingPromotion.setAppliesTo(updatedPromotion.getAppliesTo());

        try {
            Promotion savedPromotion = promotionRepository.save(existingPromotion);
            return ResponseEntity.ok(savedPromotion);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**  DELETE /api/store/promotions/{id} - Eliminar promoción (Admin) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!promotionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            promotionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}