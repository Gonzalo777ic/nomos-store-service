package com.nomos.store.service.controller;

import com.nomos.store.service.model.*;
import com.nomos.store.service.repository.AccountsReceivableRepository;
import com.nomos.store.service.repository.CreditDocumentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/store/credit-documents")
@RequiredArgsConstructor
public class CreditDocumentController {

    private final CreditDocumentRepository creditDocumentRepository;
    private final AccountsReceivableRepository arRepository;

    @Data
    public static class CreditDocumentPayload {
        private Long accountsReceivableId;
        private String type;
        private Double amount;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private String debtorName;
        private String debtorIdNumber;
        private String documentNumber;
        private String legalNotes;

        private String placeOfIssue;
        private String placeOfPayment;
        private String guarantorName;
        private String guarantorIdNumber;
    }

    @GetMapping("/ar/{arId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<CreditDocument>> getByAccount(@PathVariable Long arId) {
        return ResponseEntity.ok(creditDocumentRepository.findByAccountsReceivableId(arId));
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> create(@RequestBody CreditDocumentPayload payload) {
        AccountsReceivable ar = arRepository.findById(payload.getAccountsReceivableId())
                .orElseThrow(() -> new RuntimeException("Cuenta por cobrar no encontrada"));

        CreditDocument doc = CreditDocument.builder()
                .accountsReceivable(ar)
                .type(CreditDocumentType.valueOf(payload.getType()))
                .amount(payload.getAmount())
                .issueDate(payload.getIssueDate())
                .dueDate(payload.getDueDate())
                .debtorName(payload.getDebtorName())
                .debtorIdNumber(payload.getDebtorIdNumber())
                .creditorName("MI EMPRESA S.A.C.")
                .documentNumber(payload.getDocumentNumber())
                .status(CreditDocumentStatus.DRAFT)
                .legalNotes(payload.getLegalNotes())

                .placeOfIssue(payload.getPlaceOfIssue())
                .placeOfPayment(payload.getPlaceOfPayment())
                .guarantorName(payload.getGuarantorName())
                .guarantorIdNumber(payload.getGuarantorIdNumber())
                .build();

        return ResponseEntity.ok(creditDocumentRepository.save(doc));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return creditDocumentRepository.findById(id).map(doc -> {
            doc.setStatus(CreditDocumentStatus.valueOf(status));
            return ResponseEntity.ok(creditDocumentRepository.save(doc));
        }).orElse(ResponseEntity.notFound().build());
    }
}