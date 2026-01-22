package com.nomos.store.service.service;

import com.nomos.store.service.model.AccountingJournalEntry;
import com.nomos.store.service.model.AccountingJournalLine;
import com.nomos.store.service.repository.AccountingJournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountingService {

    @Autowired
    private AccountingJournalEntryRepository journalRepository;

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