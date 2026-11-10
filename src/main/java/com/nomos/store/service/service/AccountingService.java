package com.nomos.store.service.service;

import com.nomos.store.service.model.AccountingJournalEntry;
import com.nomos.store.service.model.AccountingJournalLine;
import com.nomos.store.service.model.Sale;
import com.nomos.store.service.repository.AccountingJournalEntryRepository;
import com.valeerp.accounting.LedgerProperties;
import com.valeerp.kernel.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountingService {

    @Autowired
    private AccountingJournalEntryRepository journalRepository;
    @Autowired
    private LedgerProperties ledgerProperties;

    @Transactional
    public void recordSale(Sale sale, Money tax, Money base) {
        AccountingJournalEntry entry = new AccountingJournalEntry();
        entry.setEntryDate(LocalDateTime.now());
        entry.setConcept("Venta " + sale.getType() + " #" + sale.getId());
        entry.setReferenceDocument("SALE-" + sale.getId());
        entry.setStatus("POSTED");
        entry.setLines(balanced(ledgerProperties.receivableCode(), ledgerProperties.receivableName(), sale.getTotalAmount().doubleValue(), 0));
        entry.getLines().add(line(ledgerProperties.taxCode(), ledgerProperties.taxName(), 0, tax.amount().doubleValue()));
        entry.getLines().add(line(ledgerProperties.revenueCode(), ledgerProperties.revenueName(), 0, base.amount().doubleValue()));
        createEntry(entry);
    }

    @Transactional
    public void reverseSale(Sale sale) {
        List<AccountingJournalEntry> entries = journalRepository.findByReferenceDocument("SALE-" + sale.getId());
        for (AccountingJournalEntry original : entries) {
            if (original.getReferenceDocument() != null && original.getReferenceDocument().startsWith("REV-")) {
                continue;
            }
            AccountingJournalEntry reversal = new AccountingJournalEntry();
            reversal.setEntryDate(LocalDateTime.now());
            reversal.setConcept("Reverso " + original.getConcept());
            reversal.setReferenceDocument("REV-" + original.getReferenceDocument());
            reversal.setStatus("POSTED");
            reversal.setLines(new java.util.ArrayList<>());
            for (AccountingJournalLine originalLine : original.getLines()) {
                reversal.getLines().add(line(originalLine.getAccountCode(), originalLine.getAccountName(),
                        originalLine.getCredit(), originalLine.getDebit()));
            }
            createEntry(reversal);
        }
    }

    private List<AccountingJournalLine> balanced(String code, String name, double debit, double credit) {
        List<AccountingJournalLine> lines = new java.util.ArrayList<>();
        lines.add(line(code, name, debit, credit));
        return lines;
    }

    private AccountingJournalLine line(String code, String name, double debit, double credit) {
        return AccountingJournalLine.builder()
                .accountCode(code)
                .accountName(name)
                .debit(debit)
                .credit(credit)
                .build();
    }

    /**
     * Registra un nuevo asiento contable en el sistema.
     * Valida estrictamente que el asiento esté cuadrado (DEBE == HABER).
     */
    @Transactional
    public AccountingJournalEntry createEntry(AccountingJournalEntry entry) {
        if (entry.getLines() == null || entry.getLines().isEmpty()) {
            throw new IllegalArgumentException("El asiento contable debe tener al menos una línea.");
        }

        for (AccountingJournalLine line : entry.getLines()) {
            line.setJournalEntry(entry);
        }

        if (!entry.isBalanced()) {
            double totalDebit = entry.getLines().stream().mapToDouble(AccountingJournalLine::getDebit).sum();
            double totalCredit = entry.getLines().stream().mapToDouble(AccountingJournalLine::getCredit).sum();

            throw new RuntimeException(String.format(
                    "Asiento Contable Descuadrado: DEBE (%.2f) != HABER (%.2f). No se puede guardar.",
                    totalDebit, totalCredit
            ));
        }

        return journalRepository.save(entry);
    }

    /**
     * Obtener historial completo ordenado por fecha.
     */
    public List<AccountingJournalEntry> getAllEntries() {
        return journalRepository.findAllByOrderByEntryDateDesc();
    }

    /**
     * Obtener un asiento específico con sus líneas.
     */
    public AccountingJournalEntry getEntryById(Long id) {
        return journalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asiento contable no encontrado: " + id));
    }

    /**
     * Buscar asientos relacionados a un documento (Ej: ver todos los movimientos de la Venta #100)
     */
    public List<AccountingJournalEntry> getEntriesByReference(String reference) {
        return journalRepository.findByReferenceDocument(reference);
    }
}