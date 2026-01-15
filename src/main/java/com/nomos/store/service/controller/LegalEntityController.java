package com.nomos.store.service.controller;

import com.nomos.store.service.model.LegalEntity;
import com.nomos.store.service.repository.LegalEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store/legal-entities")
@RequiredArgsConstructor
public class LegalEntityController {

    private final LegalEntityRepository repository;

    /**
     * MAESTRO: Listar todas las empresas/personas registradas.
     * Usado para llenar el <Select> en el formulario de Título Valor.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<LegalEntity>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    /**
     * MAESTRO: Obtener una entidad por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * MAESTRO: Crear nueva Entidad Legal.
     * Incluye validación de unicidad de RUC/DNI.
     */
    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody LegalEntity entity) {

        if (repository.existsByTaxId(entity.getTaxId())) {
            return ResponseEntity.badRequest()
                    .body("Ya existe una entidad registrada con el RUC/DNI: " + entity.getTaxId());
        }

        return ResponseEntity.ok(repository.save(entity));
    }

    /**
     * MAESTRO: Actualizar datos de la Entidad Legal.
     */
    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LegalEntity updatedData) {
        return repository.findById(id).map(existing -> {

            if (!existing.getTaxId().equals(updatedData.getTaxId()) && repository.existsByTaxId(updatedData.getTaxId())) {
                throw new IllegalArgumentException("El nuevo RUC/DNI ya pertenece a otra entidad.");
            }

            existing.setLegalName(updatedData.getLegalName());
            existing.setTaxId(updatedData.getTaxId());
            existing.setAddress(updatedData.getAddress());
            existing.setEmail(updatedData.getEmail());
            existing.setPhone(updatedData.getPhone());
            existing.setType(updatedData.getType());


            return ResponseEntity.ok(repository.save(existing));

        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * MAESTRO: Eliminar Entidad.
     */
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("No se puede eliminar esta entidad porque ya tiene documentos asociados.");
        }
    }
}