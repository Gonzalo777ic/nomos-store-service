package com.valeerp.pos;

import com.valeerp.store.service.model.PaymentMethodConfig;
import com.valeerp.store.service.model.TaxRate;
import com.valeerp.store.service.repository.PaymentMethodConfigRepository;
import com.valeerp.store.service.repository.TaxRateRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PosTicketService {

    private final PosTicketRepository ticketRepository;
    private final PosConfigRepository configRepository;
    private final PosReversalDocumentRepository reversalRepository;
    private final TaxRateRepository taxRateRepository;
    private final PaymentMethodConfigRepository paymentMethodRepository;
    private final TimeProvider timeProvider;

    public PosTicketService(PosTicketRepository ticketRepository,
                            PosConfigRepository configRepository,
                            PosReversalDocumentRepository reversalRepository,
                            TaxRateRepository taxRateRepository,
                            PaymentMethodConfigRepository paymentMethodRepository,
                            TimeProvider timeProvider) {
        this.ticketRepository = ticketRepository;
        this.configRepository = configRepository;
        this.reversalRepository = reversalRepository;
        this.taxRateRepository = taxRateRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.timeProvider = timeProvider;
    }

    public Page<PosTicket> list(Long companyId, Long posConfigId, PosTicketStatus status, Pageable pageable) {
        if (posConfigId != null) {
            return ticketRepository.findByPosConfigId(posConfigId, pageable);
        }
        if (companyId != null) {
            return ticketRepository.findByCompanyId(companyId, pageable);
        }
        if (status != null) {
            return ticketRepository.findByStatus(status, pageable);
        }
        return ticketRepository.findAll(pageable);
    }

    public PosTicket require(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Ticket POS no encontrado"));
    }

    public List<PosReversalDocument> reversalsOf(Long ticketId) {
        return reversalRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional
    public PosTicket open(PosTicketOpenRequest request) {
        PosConfig config = configRepository.findById(request.posConfigId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Config POS no encontrada"));
        PosTicket ticket = PosTicket.open(config, request.cashSessionId(), request.partnerId(), timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public PosTicket addLine(Long ticketId, PosTicketAddLineRequest request) {
        PosTicket ticket = require(ticketId);
        TaxRate taxRate = taxRateRepository.findById(request.taxRateId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Tasa de impuesto no encontrada"));
        if (taxRate.isRetired()) {
            throw new DomainException("La tasa de impuesto está dada de baja");
        }
        Money unitPrice = Money.of(request.unitPrice(), ticket.getCurrency());
        ticket.addLine(
                request.productId(),
                request.productName(),
                request.quantity(),
                unitPrice,
                taxRate.getId(),
                BigDecimal.valueOf(taxRate.getRate())
        );
        return ticketRepository.save(ticket);
    }

    @Transactional
    public PosTicket addPayment(Long ticketId, PosTicketAddPaymentRequest request) {
        PosTicket ticket = require(ticketId);
        PaymentMethodConfig method = paymentMethodRepository.findById(request.paymentMethodId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Método de pago no encontrado"));
        if (method.isRetired()) {
            throw new DomainException("El método de pago está dado de baja");
        }
        ticket.addPayment(method.getId(), Money.of(request.amount(), ticket.getCurrency()));
        return ticketRepository.save(ticket);
    }

    @Transactional
    public PosTicket post(Long ticketId) {
        PosTicket ticket = require(ticketId);
        ticket.post(timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public PosTicket voidTicket(Long ticketId, PosTicketVoidRequest request) {
        PosTicket ticket = require(ticketId);
        ticket.markVoided(request.reason(), timeProvider.now());
        PosReversalDocument reversal = PosReversalDocument.voidOf(ticket, request.reason(), timeProvider.now());
        reversalRepository.save(reversal);
        return ticketRepository.save(ticket);
    }
}
