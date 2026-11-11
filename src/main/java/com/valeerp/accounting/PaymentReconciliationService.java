package com.valeerp.accounting;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Conciliación de cobros/pagos contra facturas locales (#76).
 */
@Service
public class PaymentReconciliationService {

    private final InvoiceDocumentRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TimeProvider timeProvider;

    public PaymentReconciliationService(InvoiceDocumentRepository invoiceRepository,
                                        PaymentRepository paymentRepository,
                                        TimeProvider timeProvider) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.timeProvider = timeProvider;
    }

    public List<Payment> listPayments(Long companyId) {
        requireCompany(companyId);
        return paymentRepository.findByCompanyIdOrderByPaymentDateDescIdDesc(companyId);
    }

    public Payment requirePayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Pago no encontrado"));
    }

    @Transactional
    public Payment createPayment(PaymentCreateRequest request) {
        if (request == null) {
            throw new DomainException("La solicitud de pago es obligatoria");
        }
        String currency = blankToDefault(request.currencyCode(), Currency.PEN.code());
        Payment payment = Payment.draft(
                request.companyId(),
                request.partnerId(),
                PaymentDirection.require(request.direction()),
                request.paymentDate(),
                Money.of(request.amount(), currency),
                request.referenceNumber()
        );
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment confirmPayment(Long id) {
        Payment payment = requirePayment(id);
        payment.confirm(timeProvider.now());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment reconcile(Long paymentId, PaymentReconcileRequest request) {
        if (request == null || request.allocations() == null || request.allocations().isEmpty()) {
            throw new DomainException("Debe indicar asignaciones para conciliar");
        }
        Payment payment = requirePayment(paymentId);

        Map<Long, Money> amounts = new HashMap<>();
        for (PaymentReconcileRequest.AllocationLine line : request.allocations()) {
            if (line.invoiceId() == null) {
                throw new DomainException("Cada asignación exige invoiceId");
            }
            amounts.put(line.invoiceId(), Money.of(line.amount(), payment.getCurrencyCode()));
        }

        Map<Long, InvoiceDocument> invoices = invoiceRepository.findByIdIn(amounts.keySet()).stream()
                .collect(Collectors.toMap(InvoiceDocument::getId, Function.identity()));

        payment.reconcile(invoices, amounts);
        invoiceRepository.saveAll(invoices.values());
        return paymentRepository.save(payment);
    }

    private static void requireCompany(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La compañía es obligatoria");
        }
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
