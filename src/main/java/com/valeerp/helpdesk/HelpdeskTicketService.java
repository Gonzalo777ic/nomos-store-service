package com.valeerp.helpdesk;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.PartnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class HelpdeskTicketService {

    private final HelpdeskTicketRepository ticketRepository;
    private final HelpdeskSlaService slaService;
    private final PartnerService partnerService;
    private final TimeProvider timeProvider;

    public HelpdeskTicketService(HelpdeskTicketRepository ticketRepository,
                                 HelpdeskSlaService slaService,
                                 PartnerService partnerService,
                                 TimeProvider timeProvider) {
        this.ticketRepository = ticketRepository;
        this.slaService = slaService;
        this.partnerService = partnerService;
        this.timeProvider = timeProvider;
    }

    public Page<HelpdeskTicket> list(TicketStatus status, Long partnerId, Long assigneeUserId, Pageable pageable) {
        if (assigneeUserId != null) {
            return ticketRepository.findByAssigneeUserId(assigneeUserId, pageable);
        }
        if (partnerId != null) {
            return ticketRepository.findByPartnerId(partnerId, pageable);
        }
        if (status != null) {
            return ticketRepository.findByStatus(status, pageable);
        }
        return ticketRepository.findAll(pageable);
    }

    public HelpdeskTicket require(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Ticket no encontrado"));
    }

    @Transactional
    public HelpdeskTicket create(TicketCreateRequest request) {
        ensurePartner(request.partnerId());
        HelpdeskSla sla = resolveSla(request.slaId());
        HelpdeskTicket ticket = HelpdeskTicket.open(
                request.subject(),
                request.description(),
                TicketPriority.require(request.priority()),
                request.partnerId(),
                request.reporterUserId(),
                request.assigneeUserId(),
                sla,
                timeProvider.now()
        );
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket update(Long id, TicketUpdateRequest request) {
        HelpdeskTicket ticket = require(id);
        ensurePartner(request.partnerId());
        TicketPriority priority = request.priority() == null || request.priority().isBlank()
                ? null
                : TicketPriority.require(request.priority());
        ticket.update(
                request.subject(),
                request.description(),
                priority,
                request.partnerId(),
                request.assigneeUserId()
        );
        if (request.slaId() != null) {
            ticket.reapplySla(slaService.require(request.slaId()));
        }
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket assign(Long id, TicketAssignRequest request) {
        HelpdeskTicket ticket = require(id);
        ticket.assign(request == null ? null : request.assigneeUserId());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket startProgress(Long id) {
        HelpdeskTicket ticket = require(id);
        ticket.startProgress(timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket waitForCustomer(Long id) {
        HelpdeskTicket ticket = require(id);
        ticket.waitForCustomer(timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket resolve(Long id) {
        HelpdeskTicket ticket = require(id);
        ticket.resolve(timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket close(Long id) {
        HelpdeskTicket ticket = require(id);
        ticket.close(timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public HelpdeskTicket cancel(Long id) {
        HelpdeskTicket ticket = require(id);
        ticket.cancel(timeProvider.now());
        return ticketRepository.save(ticket);
    }

    @Transactional
    public TicketAttachment attach(Long ticketId, TicketAttachmentRequest request) {
        HelpdeskTicket ticket = require(ticketId);
        TicketAttachment attachment = ticket.attach(
                request.label(),
                request.url(),
                request.contentType(),
                request.sizeBytes(),
                request.fileName(),
                timeProvider.now()
        );
        ticketRepository.save(ticket);
        return attachment;
    }

    @Transactional
    public void retireAttachment(Long ticketId, Long attachmentId) {
        HelpdeskTicket ticket = require(ticketId);
        ticket.retireAttachment(attachmentId, Actors.currentActor(), timeProvider.now());
        ticketRepository.save(ticket);
    }

    public TicketResponse toResponse(HelpdeskTicket ticket) {
        return TicketResponse.from(ticket, timeProvider.now());
    }

    public static TicketStatus requireStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return TicketStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de ticket no soportado: " + raw);
        }
    }

    private HelpdeskSla resolveSla(Long slaId) {
        if (slaId == null) {
            return null;
        }
        return slaService.require(slaId);
    }

    private void ensurePartner(Long partnerId) {
        if (partnerId != null) {
            partnerService.require(partnerId);
        }
    }
}
