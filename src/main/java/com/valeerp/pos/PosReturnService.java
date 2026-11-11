package com.valeerp.pos;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PosReturnService {

    private final PosReturnRepository returnRepository;
    private final PosTicketRepository ticketRepository;
    private final PosReversalDocumentRepository reversalRepository;
    private final TimeProvider timeProvider;

    public PosReturnService(PosReturnRepository returnRepository,
                            PosTicketRepository ticketRepository,
                            PosReversalDocumentRepository reversalRepository,
                            TimeProvider timeProvider) {
        this.returnRepository = returnRepository;
        this.ticketRepository = ticketRepository;
        this.reversalRepository = reversalRepository;
        this.timeProvider = timeProvider;
    }

    public PosReturn require(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Devolución POS no encontrada"));
    }

    public List<PosReturn> byTicket(Long ticketId) {
        return returnRepository.findByTicketId(ticketId);
    }

    public Page<PosReturn> list(Long ticketId, Pageable pageable) {
        if (ticketId == null) {
            return returnRepository.findAll(pageable);
        }
        return returnRepository.findByTicketId(ticketId, pageable);
    }

    @Transactional
    public PosReturn createDraft(PosReturnCreateRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new DomainException("La devolución exige al menos una línea");
        }
        PosTicket ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Ticket POS no encontrado"));

        PosReturn posReturn = PosReturn.draft(ticket, request.reason(), timeProvider.now());
        for (PosReturnCreateRequest.Line requested : request.lines()) {
            PosTicketLine ticketLine = ticket.requireLine(requested.ticketLineId());
            posReturn.addLine(ticketLine, requested.quantity());
        }
        return returnRepository.save(posReturn);
    }

    @Transactional
    public PosReturn confirm(Long returnId) {
        PosReturn posReturn = require(returnId);
        if (posReturn.getStatus() != PosReturnStatus.DRAFT) {
            throw new DomainException("La devolución ya no está en borrador");
        }
        PosTicket ticket = ticketRepository.findById(posReturn.getTicketId())
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Ticket POS no encontrado"));

        for (PosReturnLine line : posReturn.getLines()) {
            PosTicketLine ticketLine = ticket.requireLine(line.getTicketLineId());
            ticketLine.registerReturn(line.getQuantity());
        }
        posReturn.confirm(timeProvider.now());
        ticket.applyConfirmedReturn();

        PosReversalDocument reversal = PosReversalDocument.returnOf(
                ticket,
                posReturn.getId(),
                posReturn.refundMoney(),
                posReturn.getReason(),
                timeProvider.now()
        );
        reversalRepository.save(reversal);
        ticketRepository.save(ticket);
        return returnRepository.save(posReturn);
    }

    @Transactional
    public PosReturn cancel(Long returnId) {
        PosReturn posReturn = require(returnId);
        posReturn.cancel();
        return returnRepository.save(posReturn);
    }
}
