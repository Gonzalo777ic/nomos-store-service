package com.nomos.store.service.service;

import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SaleReturnService {

    @Autowired
    private SaleReturnRepository returnRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private SaleDetailRepository saleDetailRepository;
    @Autowired
    private SalesDocumentService salesDocumentService;

    /**
     * CREAR BORRADOR
     * Recibe los datos crudos desde el controller
     */
    @Transactional
    public SaleReturn createDraft(Long saleId, String reason, SaleReturnType type, Map<Long, Integer> itemsToReturn) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        SaleReturn saleReturn = SaleReturn.builder()
                .sale(sale)
                .returnDate(LocalDateTime.now())
                .type(type)
                .status(SaleReturnStatus.DRAFT)
                .reason(reason)
                .totalRefundAmount(0.0)
                .details(new ArrayList<>())
                .build();

        double totalAmount = 0.0;

        for (Map.Entry<Long, Integer> entry : itemsToReturn.entrySet()) {
            Long originalDetailId = entry.getKey();
            Integer quantityToReturn = entry.getValue();

            if (quantityToReturn <= 0) continue;

            SaleDetail originalDetail = saleDetailRepository.findById(originalDetailId)
                    .orElseThrow(() -> new RuntimeException("Detalle de venta original no encontrado: " + originalDetailId));



            if (quantityToReturn > originalDetail.getQuantity()) {
                throw new RuntimeException("No puedes devolver más cantidad de la vendida para el producto: " + originalDetail.getProductId());
            }

            double rowTotal = originalDetail.getUnitPrice() * quantityToReturn;
            totalAmount += rowTotal;

            SaleReturnDetail returnDetail = SaleReturnDetail.builder()
                    .saleReturn(saleReturn)
                    .originalSaleDetail(originalDetail)
                    .quantity(quantityToReturn)
                    .unitPrice(originalDetail.getUnitPrice())
                    .subtotal(rowTotal)
                    .build();

            saleReturn.getDetails().add(returnDetail);
        }

        saleReturn.setTotalRefundAmount(totalAmount);

        return returnRepository.save(saleReturn);
    }

    /**
     * CONFIRMAR DEVOLUCIÓN
     * Genera la Nota de Crédito y mueve inventario
     */
    @Transactional
    public SaleReturn confirmReturn(Long returnId) {
        try {

            SaleReturn saleReturn = returnRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("Devolución no encontrada"));

            if (saleReturn.getStatus() != SaleReturnStatus.DRAFT) {
                return saleReturn;
            }

            saleReturn.setStatus(SaleReturnStatus.CONFIRMED);


            SalesDocument creditNote = salesDocumentService.issueCreditNote(
                    saleReturn.getSale().getId(),
                    saleReturn.getTotalRefundAmount(),
                    "Devolución: " + saleReturn.getReason()
            );

            saleReturn.setCreditNote(creditNote);

            return returnRepository.save(saleReturn);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Error fatal confirmando devolución: " + e.getMessage());
        }
    }

    public List<SaleReturn> findBySaleId(Long saleId) {
        return returnRepository.findBySaleId(saleId);
    }
}