package com.nomos.store.service.controller;

import com.nomos.store.service.model.TaxRate;
import com.nomos.store.service.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/store/tax-rates")
@RequiredArgsConstructor
public class TaxRateController {

    private final TaxRateRepository taxRateRepository;

    /**
     * 🔑 ENDPOINT: GET /api/store/tax-rates
     * Obtiene la lista completa de todas las tasas de impuesto configuradas.
     * Permite a todos los roles internos y vendedores obtener la lista de tasas.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public List<TaxRate> getAllTaxRates() {
        return taxRateRepository.findAll();
    }

    /**
     * 🔑 ENDPOINT: POST /api/store/tax-rates
     * Solo los Administradores pueden crear nuevas tasas.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TaxRate> createTaxRate(@RequestBody TaxRate taxRate) {
        try {
            TaxRate savedRate = taxRateRepository.save(taxRate);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRate);
        } catch (DataIntegrityViolationException e) {
            // Manejo de error de unicidad (409 Conflict)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TaxRate> updateTaxRate(@PathVariable Long id, @RequestBody TaxRate updatedRate) {
        Optional<TaxRate> existingRateOpt = taxRateRepository.findById(id);

        if (existingRateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TaxRate existingRate = existingRateOpt.get();

        // 🚨 Solo actualizamos la tasa. Mantenemos el nombre y el ID.
        existingRate.setRate(updatedRate.getRate());

        try {
            TaxRate savedRate = taxRateRepository.save(existingRate);
            return ResponseEntity.ok(savedRate);
        } catch (DataIntegrityViolationException e) {
            // Esto no debería pasar ya que el nombre no se cambia, pero por seguridad
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    /**
     * 🔑 ENDPOINT: DELETE /api/store/tax-rates/{id}
     * Solo los Administradores pueden eliminar tasas.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTaxRate(@PathVariable Long id) {
        if (!taxRateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taxRateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Opcionalmente, se podría añadir un PUT para editar, pero por simplicidad de un valor fijo,
    // a menudo se prefiere eliminar y recrear si el valor es incorrecto.
}