package com.nomos.store.service.controller;

import com.nomos.store.service.model.Collection;
import com.nomos.store.service.model.PaymentMethodConfig;
import com.nomos.store.service.model.Sale;
import com.nomos.store.service.repository.CollectionRepository;
import com.nomos.store.service.repository.PaymentMethodConfigRepository;
import com.nomos.store.service.repository.SaleRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/store/collections")
public class CollectionController {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PaymentMethodConfigRepository paymentMethodRepository;

    @Data
    public static class CollectionPayload {
        private Long saleId;
        private Double amount;
        private Long paymentMethodId;
        private String referenceNumber;
        private LocalDateTime collectionDate;
    }

    @PostMapping
    public ResponseEntity<Collection> createCollection(@RequestBody CollectionPayload payload) {
        Sale sale = saleRepository.findById(payload.getSaleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        PaymentMethodConfig paymentMethod = paymentMethodRepository.findById(payload.getPaymentMethodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));

        if (payload.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a 0");
        }

        Collection collection = Collection.builder()
                .sale(sale)
                .amount(payload.getAmount())
                .paymentMethod(paymentMethod)
                .referenceNumber(payload.getReferenceNumber())
                .collectionDate(payload.getCollectionDate() != null ? payload.getCollectionDate() : LocalDateTime.now())
                .build();

        Collection savedCollection = collectionRepository.save(collection);
        return new ResponseEntity<>(savedCollection, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Collection>> getAllCollections() {
        return ResponseEntity.ok(collectionRepository.findAll());
    }

    @GetMapping("/sale/{saleId}")
    public ResponseEntity<List<Collection>> getCollectionsBySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(collectionRepository.findBySaleId(saleId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cobro no encontrado");
        }
        collectionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}