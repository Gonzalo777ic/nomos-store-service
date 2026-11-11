package com.valeerp.accounting;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso de asientos (#71/#77). Rechaza desbalance en el agregado; no hay delete de publicados.
 */
@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalService journalService;
    private final PeriodLockService periodLockService;
    private final TimeProvider timeProvider;

    public JournalEntryService(JournalEntryRepository journalEntryRepository,
                               JournalService journalService,
                               PeriodLockService periodLockService,
                               TimeProvider timeProvider) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalService = journalService;
        this.periodLockService = periodLockService;
        this.timeProvider = timeProvider;
    }

    public Page<JournalEntry> list(Long companyId, Pageable pageable) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return journalEntryRepository.findByCompanyIdOrderByEntryDateDescIdDesc(companyId, pageable);
    }

    public JournalEntry require(Long id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Asiento no encontrado"));
    }

    public List<JournalEntry> byReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new DomainException("La referencia es obligatoria");
        }
        return journalEntryRepository.findByReferenceDocumentOrderByIdAsc(reference.trim());
    }

    @Transactional
    public JournalEntry createDraft(JournalEntryDraftRequest request) {
        Journal journal = journalService.require(request.journalId());
        if (!journal.getCompanyId().equals(request.companyId())) {
            throw new DomainException("El diario no pertenece a la compañía del asiento");
        }
        String currency = resolveCurrency(request.currencyCode());
        JournalEntry entry = JournalEntry.draft(
                request.companyId(),
                request.journalId(),
                request.entryDate(),
                request.concept(),
                request.referenceDocument(),
                currency
        );
        appendLines(entry, request.lines(), currency);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public JournalEntry post(Long id) {
        JournalEntry entry = require(id);
        entry.post(date -> periodLockService.assertJournalsOpen(entry.getCompanyId(), date), timeProvider.now());
        return journalEntryRepository.save(entry);
    }

    /**
     * Crea borrador balanceado y lo publica en un solo paso (stub interno de facturas).
     */
    @Transactional
    public JournalEntry proposeAndPost(JournalEntryDraftRequest request) {
        JournalEntry draft = createDraft(request);
        draft.post(date -> periodLockService.assertJournalsOpen(draft.getCompanyId(), date), timeProvider.now());
        return journalEntryRepository.save(draft);
    }

    @Transactional
    public JournalEntry voidPosted(Long id, LocalDate reversalDate) {
        JournalEntry entry = require(id);
        JournalEntry reversal = entry.voidPosted(
                date -> periodLockService.assertJournalsOpen(entry.getCompanyId(), date),
                Actors.currentActor(),
                timeProvider.now(),
                reversalDate
        );
        JournalEntry savedReversal = journalEntryRepository.save(reversal);
        entry.linkReversal(savedReversal.getId());
        journalEntryRepository.save(entry);
        return savedReversal;
    }

    private void appendLines(JournalEntry entry, List<JournalEntryLineDraft> lines, String currency) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("El asiento requiere al menos una línea");
        }
        for (JournalEntryLineDraft line : lines) {
            Money debit = Money.of(line.debit() == null ? java.math.BigDecimal.ZERO : line.debit(), currency);
            Money credit = Money.of(line.credit() == null ? java.math.BigDecimal.ZERO : line.credit(), currency);
            entry.addLine(
                    line.accountCode(),
                    line.accountName(),
                    AccountClass.require(line.accountClass()),
                    debit,
                    credit,
                    line.memo()
            );
        }
    }

    private static String resolveCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return AccountingPolicy.DEFAULT_CURRENCY.code();
        }
        return currencyCode.trim().toUpperCase();
    }
}
