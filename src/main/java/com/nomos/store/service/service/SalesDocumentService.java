package com.nomos.store.service.service;

import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.SaleRepository;
import com.nomos.store.service.repository.SalesDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SalesDocumentService {

    @Autowired
    private SalesDocumentRepository documentRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SalesDocumentPdfService pdfService;

    /**
     * EMISIÓN: Crea el documento lógico en base de datos.
     * No genera el PDF aquí, solo prepara los datos y asigna correlativos.
     */
    @Transactional
    public SalesDocument issueDocument(Long saleId, SalesDocumentType type) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + saleId));

        String series = (type == SalesDocumentType.FACTURA) ? "F001" : "B001";

        long count = documentRepository.countBySeries(series);
        String number = String.format("%08d", count + 1);

        SalesDocument doc = SalesDocument.builder()
                .sale(sale)
                .type(type)
                .series(series)
                .number(number)
                .issueDate(LocalDateTime.now())
                .status(SalesDocumentStatus.ISSUED)
                .totalAmount(sale.getTotalAmount())
                .build();

        return documentRepository.save(doc);
    }

    /**
     * GENERACIÓN: Busca el documento y pide al especialista visual que dibuje.
     */
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long documentId) {
        SalesDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + documentId));

        return pdfService.generatePdf(doc);
    }


    @Transactional
    public SalesDocument issueCreditNote(Long saleId, Double amount, String reason) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        boolean isFactura = "FACTURA".equals(sale.getType().name());
        String series = isFactura ? "FC01" : "BC01";

        long count = documentRepository.countBySeries(series);
        String number = String.format("%08d", count + 1);

        SalesDocument doc = SalesDocument.builder()
                .sale(sale)
                .type(SalesDocumentType.NOTA_CREDITO)
                .series(series)
                .number(number)
                .issueDate(LocalDateTime.now())
                .status(SalesDocumentStatus.ISSUED)
                .totalAmount(amount)
                .responseMessage(reason)
                .build();

        return documentRepository.save(doc);
    }
}