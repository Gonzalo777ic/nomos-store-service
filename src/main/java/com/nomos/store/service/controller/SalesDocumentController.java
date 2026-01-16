package com.nomos.store.service.controller;

import com.nomos.store.service.model.SalesDocument;
import com.nomos.store.service.model.SalesDocumentType;
import com.nomos.store.service.service.SalesDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-documents")
public class SalesDocumentController {

    @Autowired
    private SalesDocumentService service;

    /**
     * Endpoint para EMITIR (Crear) el documento.
     * POST /api/sales-documents/issue?saleId=123&type=FACTURA
     */
    @PostMapping("/issue")
    public ResponseEntity<SalesDocument> issue(
            @RequestParam Long saleId,
            @RequestParam SalesDocumentType type
    ) {
        SalesDocument doc = service.issueDocument(saleId, type);
        return ResponseEntity.ok(doc);
    }

    /**
     * Endpoint para DESCARGAR el PDF.
     * GET /api/sales-documents/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        byte[] pdfBytes = service.generatePdf(id);




        String filename = "comprobante_" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}