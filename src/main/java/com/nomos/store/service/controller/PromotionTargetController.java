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

    /** 🔑 GET /api/store/promotion-targets/{promotionId} - Obtiene todos los targets para una promoción */
    @GetMapping("/by-promotion/{promotionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PromotionTarget>> getTargetsByPromotion(@PathVariable Long promotionId) {
        return ResponseEntity.ok(targetRepository.findByPromotionId(promotionId));
    }

    /** 🔑 POST /api/store/promotion-targets/bulk-update/{promotionId}
     * Actualiza masivamente los targets de una promoción (Elimina viejos, crea nuevos).
     * Requiere transaccionalidad.
     */
    @PostMapping("/bulk-update/{promotionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional // Para asegurar que la eliminación y creación sean atómicas
    public ResponseEntity<List<PromotionTarget>> bulkUpdateTargets(
            @PathVariable Long promotionId,
            @RequestBody List<PromotionTarget> newTargets) {

        // 1. Eliminar los targets existentes para esta promoción
        targetRepository.deleteByPromotionId(promotionId);

        // 2. Asignar la FK y guardar los nuevos targets
        newTargets.forEach(target -> target.setPromotionId(promotionId));

        try {
            List<PromotionTarget> savedTargets = targetRepository.saveAll(newTargets);
            return ResponseEntity.ok(savedTargets);
        } catch (DataIntegrityViolationException e) {
            // Esto captura si un target_id o target_type es inválido (aunque JPA no lo valida a menos que sea FK)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /** 🔑 DELETE /api/store/promotion-targets/{id} - Elimina un target individual */
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