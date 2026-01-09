package com.nomos.store.service.controller;

import com.nomos.store.service.model.PromotionTarget;
import com.nomos.store.service.repository.PromotionTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/promotion-targets")
@RequiredArgsConstructor
public class PromotionTargetController {

    private final PromotionTargetRepository targetRepository;

    /**  GET /api/store/promotion-targets/{promotionId} - Obtiene todos los targets para una promoción */
    @GetMapping("/by-promotion/{promotionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PromotionTarget>> getTargetsByPromotion(@PathVariable Long promotionId) {
        return ResponseEntity.ok(targetRepository.findByPromotionId(promotionId));
    }

    /**  POST /api/store/promotion-targets/bulk-update/{promotionId}
     * Actualiza masivamente los targets de una promoción (Elimina viejos, crea nuevos).
     * Requiere transaccionalidad.
     */
    @PostMapping("/bulk-update/{promotionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<List<PromotionTarget>> bulkUpdateTargets(
            @PathVariable Long promotionId,
            @RequestBody List<PromotionTarget> newTargets) {
        targetRepository.deleteByPromotionId(promotionId);
        newTargets.forEach(target -> target.setPromotionId(promotionId));

        try {
            List<PromotionTarget> savedTargets = targetRepository.saveAll(newTargets);
            return ResponseEntity.ok(savedTargets);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**  DELETE /api/store/promotion-targets/{id} - Elimina un target individual */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTarget(@PathVariable Long id) {
        if (!targetRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        targetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}