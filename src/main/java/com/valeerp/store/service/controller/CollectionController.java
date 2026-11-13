package com.valeerp.store.service.controller;

import com.valeerp.store.service.model.AccountsReceivable;
import com.valeerp.store.service.model.Collection;
import com.valeerp.store.service.model.CollectionStatus;
import com.valeerp.store.service.model.PaymentMethodConfig;
import com.valeerp.store.service.model.Sale;
import com.valeerp.store.service.model.SaleStatus;
import com.valeerp.store.service.repository.AccountsReceivableRepository;
import com.valeerp.store.service.repository.CollectionRepository;
import com.valeerp.store.service.repository.PaymentMethodConfigRepository;
import com.valeerp.store.service.repository.SaleRepository;
import com.valeerp.store.service.service.CashMovementService;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.sales.SalesPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/store/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final SaleRepository saleRepository;
    private final PaymentMethodConfigRepository paymentMethodRepository;
    private final AccountsReceivableRepository accountsReceivableRepository;
    private final CashMovementService cashMovementService;

    public CollectionController(CollectionRepository collectionRepository,
                                SaleRepository saleRepository,
                                PaymentMethodConfigRepository paymentMethodRepository,
                                AccountsReceivableRepository accountsReceivableRepository,
                                CashMovementService cashMovementService) {
        this.collectionRepository = collectionRepository;
        this.saleRepository = saleRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.accountsReceivableRepository = accountsReceivableRepository;
        this.cashMovementService = cashMovementService;
    }

    public record CollectionPayload(
            Long saleId,
            BigDecimal amount,
            Long paymentMethodId,
            String referenceNumber,
            LocalDateTime collectionDate
    ) {
    }

    public record CollectionResponse(
            Long id,
            Long saleId,
            BigDecimal amount,
            String currencyCode,
            String status,
            LocalDateTime collectionDate,
            String referenceNumber
    ) {
        static CollectionResponse from(Collection collection) {
            return new CollectionResponse(
                    collection.getId(),
                    collection.getSale().getId(),
                    collection.getAmount(),
                    collection.getCurrencyCode(),
                    collection.getStatus().name(),
                    collection.getCollectionDate(),
                    collection.getReferenceNumber()
            );
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CollectionResponse> createAndApplyCollection(@RequestBody CollectionPayload payload) {
        Sale sale = saleRepository.findById(payload.saleId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Venta no encontrada"));
        AccountsReceivable ar = sale.getAccountsReceivable();
        if (ar == null) {
            throw new DomainException("La venta no tiene cuenta por cobrar asociada");
        }
        PaymentMethodConfig paymentMethod = paymentMethodRepository.findById(payload.paymentMethodId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Método de pago no encontrado"));
        if (sale.getStatus() == SaleStatus.CANCELADA) {
            throw new DomainException("No se puede registrar cobros en una venta CANCELADA");
        }
        String currency = ar.getCurrencyCode() == null ? SalesPolicy.DEFAULT_CURRENCY : ar.getCurrencyCode();
        Collection collection = Collection.confirm(
                sale,
                ar,
                Money.of(payload.amount(), currency),
                paymentMethod,
                payload.referenceNumber(),
                payload.collectionDate()
        );
        collection = collectionRepository.saveAndFlush(collection);
        ar.applyPayment(collection, null);
        cashMovementService.registerIncomeFromCollection(collection, 1L);
        accountsReceivableRepository.saveAndFlush(ar);
        return new ResponseEntity<>(CollectionResponse.from(collection), HttpStatus.CREATED);
    }

    @GetMapping
    public List<CollectionResponse> getAllCollections() {
        return collectionRepository.findAll().stream().map(CollectionResponse::from).toList();
    }

    @GetMapping("/sale/{saleId}")
    public List<CollectionResponse> getCollectionsBySale(@PathVariable Long saleId) {
        return collectionRepository.findBySaleId(saleId).stream().map(CollectionResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cobro no encontrado"));
        if (collection.getStatus() == CollectionStatus.CONFIRMED) {
            throw new DomainException("Un cobro confirmado se revierte con una nota de crédito");
        }
        collection.cancel();
        collectionRepository.save(collection);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    public CollectionResponse confirmCollection(@PathVariable Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cobro no encontrado"));
        collection.markConfirmed();
        AccountsReceivable ar = collection.getAccountsReceivable();
        ar.applyPayment(collection, null);
        cashMovementService.registerIncomeFromCollection(collection, 1L);
        accountsReceivableRepository.saveAndFlush(ar);
        return CollectionResponse.from(collection);
    }
}
