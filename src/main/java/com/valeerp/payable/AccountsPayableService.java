package com.valeerp.payable;

import com.valeerp.accounting.InvoiceDocument;
import com.valeerp.accounting.InvoiceDocumentService;
import com.valeerp.accounting.InvoiceSide;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class AccountsPayableService {

    private final AccountsPayableRepository accountsPayableRepository;
    private final InvoiceDocumentService invoiceDocumentService;
    private final TimeProvider timeProvider;

    public AccountsPayableService(AccountsPayableRepository accountsPayableRepository,
                                  InvoiceDocumentService invoiceDocumentService,
                                  TimeProvider timeProvider) {
        this.accountsPayableRepository = accountsPayableRepository;
        this.invoiceDocumentService = invoiceDocumentService;
        this.timeProvider = timeProvider;
    }

    public Page<AccountsPayable> list(Long companyId, Pageable pageable) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
        return accountsPayableRepository.findByCompanyId(companyId, pageable);
    }

    public AccountsPayable require(Long id) {
        return accountsPayableRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Cuenta por pagar no encontrada"));
    }

    @Transactional
    public AccountsPayable openFromSupplierInvoice(Long supplierInvoiceId) {
        InvoiceDocument invoice = invoiceDocumentService.require(supplierInvoiceId);
        if (invoice.getSide() != InvoiceSide.VENDOR) {
            throw new DomainException("Solo una factura proveedor abre CxP");
        }
        accountsPayableRepository.findBySupplierInvoiceId(supplierInvoiceId).ifPresent(existing -> {
            throw new DomainException("Ya existe CxP para la factura " + supplierInvoiceId);
        });
        AccountsPayable payable = AccountsPayable.open(
                invoice.getCompanyId(),
                invoice.getPartnerId(),
                invoice.getId(),
                invoice.getDocumentNumber(),
                invoice.getIssueDate(),
                invoice.total()
        );
        payable.scheduleSingleInstallment(invoice.getDueDate());
        return accountsPayableRepository.save(payable);
    }

    @Transactional
    public AccountsPayable applyPayment(Long id, LocalDate paymentDate, BigDecimal amount, String referenceNumber) {
        AccountsPayable payable = require(id);
        payable.applyPayment(
                paymentDate == null ? LocalDate.now() : paymentDate,
                Money.of(amount, payable.getCurrencyCode()),
                referenceNumber
        );
        return accountsPayableRepository.save(payable);
    }

    @Transactional
    public AccountsPayable cancelPayment(Long id, Long paymentId) {
        AccountsPayable payable = require(id);
        payable.cancelPayment(paymentId);
        return accountsPayableRepository.save(payable);
    }

    /**
     * Anula por reverso: cancela la factura proveedor (contra-asiento) y marca la CxP VOIDED.
     */
    @Transactional
    public AccountsPayable voidByReversal(Long id) {
        AccountsPayable payable = require(id);
        if (payable.paidAmount().isPositive()) {
            throw new DomainException("No se anula una CxP con pagos confirmados; cancele pagos primero");
        }
        Long reversalEntryId = null;
        if (payable.getSupplierInvoiceId() != null) {
            InvoiceDocument voided = invoiceDocumentService.voidByReversal(payable.getSupplierInvoiceId());
            reversalEntryId = voided.getProposedJournalEntryId();
        }
        payable.voidByReversal(timeProvider.now(), reversalEntryId);
        return accountsPayableRepository.save(payable);
    }
}
