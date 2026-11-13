package com.valeerp.store.service.service;

import com.valeerp.store.service.model.Sale;
import com.valeerp.store.service.model.SaleDetail;
import com.valeerp.store.service.model.SaleReturn;
import com.valeerp.store.service.model.SaleReturnStatus;
import com.valeerp.store.service.model.SaleReturnType;
import com.valeerp.store.service.model.SalesDocument;
import com.valeerp.store.service.repository.SaleDetailRepository;
import com.valeerp.store.service.repository.SaleRepository;
import com.valeerp.store.service.repository.SaleReturnRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.sales.SalesPolicy;
import com.valeerp.sales.StockClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RMA / devoluciones y notas de crédito. Confirmación genera reverso; anulación no borra filas.
 */
@Service
public class SaleReturnService {

    private final SaleReturnRepository returnRepository;
    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final SalesDocumentService salesDocumentService;
    private final StockClient stockClient;
    private final String currency;

    public SaleReturnService(SaleReturnRepository returnRepository,
                             SaleRepository saleRepository,
                             SaleDetailRepository saleDetailRepository,
                             SalesDocumentService salesDocumentService,
                             StockClient stockClient,
                             @Value("${valeerp.currency:PEN}") String currency) {
        this.returnRepository = returnRepository;
        this.saleRepository = saleRepository;
        this.saleDetailRepository = saleDetailRepository;
        this.salesDocumentService = salesDocumentService;
        this.stockClient = stockClient;
        this.currency = currency == null || currency.isBlank() ? SalesPolicy.DEFAULT_CURRENCY.code() : currency;
    }

    @Transactional
    public SaleReturn createDraft(Long saleId, String reason, SaleReturnType type, Map<Long, Integer> itemsToReturn) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Venta no encontrada"));
        SaleReturn saleReturn = SaleReturn.draft(sale, type, reason, currency, LocalDateTime.now());

        for (Map.Entry<Long, Integer> entry : itemsToReturn.entrySet()) {
            Integer quantity = entry.getValue();
            if (quantity == null || quantity <= 0) {
                continue;
            }
            SaleDetail original = saleDetailRepository.findById(entry.getKey())
                    .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE,
                            "Detalle de venta no encontrado: " + entry.getKey()));
            // Subtotal del cliente ignorado: reverso proporcional al neto persistido de la línea.
            Money unitNet = unitNetOf(original);
            saleReturn.addDetail(original, quantity, unitNet);
        }
        if (saleReturn.getDetails().isEmpty()) {
            throw new DomainException("La devolución exige al menos una línea");
        }
        return returnRepository.save(saleReturn);
    }

    @Transactional
    public SaleReturn confirmReturn(Long returnId) {
        SaleReturn saleReturn = require(returnId);
        if (saleReturn.getStatus() != SaleReturnStatus.DRAFT) {
            throw new DomainException("La devolución ya no está en borrador");
        }
        stockClient.restock(
                saleReturn.getId(),
                "Devolución " + saleReturn.getId(),
                saleReturn.getDetails().stream()
                        .map(detail -> new StockClient.StockLine(
                                detail.getOriginalSaleDetail().getProductId(),
                                detail.getQuantity()))
                        .toList()
        );
        if (saleReturn.getSale().getAccountsReceivable() != null) {
            saleReturn.getSale().getAccountsReceivable().applyCredit(saleReturn.getTotalRefundAmount());
        }
        SalesDocument creditNote = salesDocumentService.issueCreditNote(
                saleReturn.getSale().getId(),
                saleReturn.getTotalRefundAmount(),
                "Devolución: " + saleReturn.getReason()
        );
        saleReturn.confirm(creditNote);
        return returnRepository.save(saleReturn);
    }

    @Transactional
    public SaleReturn reverseConfirmed(Long returnId) {
        SaleReturn saleReturn = require(returnId);
        saleReturn.reverse();
        return returnRepository.save(saleReturn);
    }

    @Transactional
    public SaleReturn cancelDraft(Long returnId) {
        SaleReturn saleReturn = require(returnId);
        saleReturn.cancelDraft();
        return returnRepository.save(saleReturn);
    }

    public SaleReturn require(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Devolución no encontrada"));
    }

    public List<SaleReturn> findBySaleId(Long saleId) {
        return returnRepository.findBySaleId(saleId);
    }

    public List<SaleReturn> findAll() {
        return returnRepository.findAll();
    }

    private Money unitNetOf(SaleDetail original) {
        BigDecimal qty = BigDecimal.valueOf(original.getQuantity());
        BigDecimal unit = original.getSubtotal().divide(qty, Money.SCALE, RoundingMode.HALF_UP);
        return Money.of(unit, currency);
    }
}
