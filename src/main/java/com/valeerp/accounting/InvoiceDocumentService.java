package com.valeerp.accounting;

import com.valeerp.store.service.model.TaxRate;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.payable.AccountsPayable;
import com.valeerp.payable.AccountsPayableRepository;
import com.valeerp.sales.TaxRateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Facturas locales que proponen asiento vía {@link JournalProposalPort} (sin Feign).
 */
@Service
public class InvoiceDocumentService {

    private final InvoiceDocumentRepository invoiceDocumentRepository;
    private final JournalProposalPort journalProposalPort;
    private final JournalService journalService;
    private final JournalEntryService journalEntryService;
    private final TaxRateService taxRateService;
    private final LedgerProperties ledgerProperties;
    private final AccountsPayableRepository accountsPayableRepository;

    public InvoiceDocumentService(InvoiceDocumentRepository invoiceDocumentRepository,
                                  JournalProposalPort journalProposalPort,
                                  JournalService journalService,
                                  JournalEntryService journalEntryService,
                                  TaxRateService taxRateService,
                                  LedgerProperties ledgerProperties,
                                  AccountsPayableRepository accountsPayableRepository) {
        this.invoiceDocumentRepository = invoiceDocumentRepository;
        this.journalProposalPort = journalProposalPort;
        this.journalService = journalService;
        this.journalEntryService = journalEntryService;
        this.taxRateService = taxRateService;
        this.ledgerProperties = ledgerProperties;
        this.accountsPayableRepository = accountsPayableRepository;
    }

    public Page<InvoiceDocument> list(Long companyId, InvoiceSide side, Pageable pageable) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        if (side == null) {
            return invoiceDocumentRepository.findByCompanyId(companyId, pageable);
        }
        return invoiceDocumentRepository.findByCompanyIdAndSide(companyId, side, pageable);
    }

    public InvoiceDocument require(Long id) {
        return invoiceDocumentRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Factura no encontrada"));
    }

    @Transactional
    public InvoiceDocument createDraft(InvoiceDocumentCreateRequest request) {
        InvoiceSide side = InvoiceSide.require(request.side());
        String currency = resolveCurrency(request.currencyCode());
        TaxRate taxRate = request.taxRateId() == null ? null : taxRateService.require(request.taxRateId());
        Money untaxed = Money.of(requireAmount(request.untaxedAmount(), "base imponible"), currency);
        Money tax = Money.of(requireAmount(request.taxAmount(), "impuesto"), currency);
        Money total = request.totalAmount() == null
                ? untaxed.plus(tax)
                : Money.of(request.totalAmount(), currency);
        if (taxRate != null && taxRate.getKind() != null) {
            assertTaxConsistent(taxRate, untaxed, tax);
        }
        InvoiceDocument invoice = InvoiceDocument.draft(
                request.companyId(),
                request.partnerId(),
                side,
                request.documentNumber(),
                request.issueDate(),
                request.dueDate(),
                taxRate == null ? null : taxRate.getId(),
                untaxed,
                tax,
                total
        );
        return invoiceDocumentRepository.save(invoice);
    }

    @Transactional
    public InvoiceDocument post(Long id) {
        InvoiceDocument invoice = require(id);
        JournalKind kind = invoice.getSide() == InvoiceSide.CUSTOMER ? JournalKind.SALES : JournalKind.PURCHASES;
        Journal journal = journalService.requireByKind(invoice.getCompanyId(), kind);
        JournalEntry entry = journalProposalPort.proposeAndPost(buildProposal(invoice, journal.getId()));
        invoice.markPosted(entry.getId());
        InvoiceDocument saved = invoiceDocumentRepository.save(invoice);
        if (saved.getSide() == InvoiceSide.VENDOR) {
            openPayableIfAbsent(saved);
        }
        return saved;
    }

    private void openPayableIfAbsent(InvoiceDocument invoice) {
        if (accountsPayableRepository.findBySupplierInvoiceId(invoice.getId()).isPresent()) {
            return;
        }
        AccountsPayable payable = AccountsPayable.open(
                invoice.getCompanyId(),
                invoice.getPartnerId(),
                invoice.getId(),
                invoice.getDocumentNumber(),
                invoice.getIssueDate(),
                invoice.total()
        );
        payable.scheduleSingleInstallment(invoice.getDueDate());
        accountsPayableRepository.save(payable);
    }

    /**
     * Anulación por reverso: cancela el documento y genera contra-asiento del propuesto.
     */
    @Transactional
    public InvoiceDocument voidByReversal(Long id) {
        InvoiceDocument invoice = require(id);
        Long originalEntryId = invoice.getProposedJournalEntryId();
        invoice.cancel();
        if (originalEntryId != null) {
            JournalEntry reversal = journalEntryService.voidPosted(originalEntryId, invoice.getIssueDate());
            invoice.linkReversalEntry(reversal.getId());
        }
        return invoiceDocumentRepository.save(invoice);
    }

    private JournalEntryDraftRequest buildProposal(InvoiceDocument invoice, Long journalId) {
        String currency = invoice.getCurrencyCode();
        List<JournalEntryLineDraft> lines = new ArrayList<>();
        if (invoice.getSide() == InvoiceSide.CUSTOMER) {
            lines.add(line(ledgerProperties.receivableCode(), ledgerProperties.receivableName(),
                    AccountClass.ASSET, invoice.total().amount(), BigDecimal.ZERO, currency, "CxC"));
            if (invoice.tax().isPositive()) {
                lines.add(line(ledgerProperties.taxCode(), ledgerProperties.taxName(),
                        AccountClass.LIABILITY, BigDecimal.ZERO, invoice.tax().amount(), currency, "IGV"));
            }
            lines.add(line(ledgerProperties.revenueCode(), ledgerProperties.revenueName(),
                    AccountClass.INCOME, BigDecimal.ZERO, invoice.untaxed().amount(), currency, "Ingreso"));
        } else {
            lines.add(line(ledgerProperties.expenseCode(), ledgerProperties.expenseName(),
                    AccountClass.EXPENSE, invoice.untaxed().amount(), BigDecimal.ZERO, currency, "Gasto"));
            if (invoice.tax().isPositive()) {
                lines.add(line(ledgerProperties.taxCode(), ledgerProperties.taxName(),
                        AccountClass.ASSET, invoice.tax().amount(), BigDecimal.ZERO, currency, "IGV crédito"));
            }
            lines.add(line(ledgerProperties.payableCode(), ledgerProperties.payableName(),
                    AccountClass.LIABILITY, BigDecimal.ZERO, invoice.total().amount(), currency, "CxP"));
        }
        String concept = (invoice.getSide() == InvoiceSide.CUSTOMER ? "Factura cliente " : "Factura proveedor ")
                + invoice.getDocumentNumber();
        return new JournalEntryDraftRequest(
                invoice.getCompanyId(),
                journalId,
                invoice.getIssueDate(),
                concept,
                invoice.getSide().name() + "-" + invoice.getDocumentNumber(),
                currency,
                lines
        );
    }

    private static JournalEntryLineDraft line(String code, String name, AccountClass accountClass,
                                              BigDecimal debit, BigDecimal credit, String currency, String memo) {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new DomainException("Configure valeerp.ledger.* con códigos de cuenta para proponer asientos");
        }
        return new JournalEntryLineDraft(code, name, accountClass.name(), debit, credit, memo);
    }

    private static void assertTaxConsistent(TaxRate taxRate, Money untaxed, Money tax) {
        Money expectedTax = untaxed.times(taxRate.rateAsDecimal());
        if (expectedTax.amount().subtract(tax.amount()).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new DomainException("El impuesto no coincide con la tasa configurada " + taxRate.getName());
        }
    }

    private static BigDecimal requireAmount(BigDecimal amount, String label) {
        if (amount == null) {
            throw new DomainException("El monto de " + label + " es obligatorio");
        }
        return amount;
    }

    private static String resolveCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return AccountingPolicy.DEFAULT_CURRENCY.code();
        }
        return currencyCode.trim().toUpperCase();
    }
}
