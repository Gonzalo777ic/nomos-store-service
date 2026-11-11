package com.valeerp.helpdesk;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long id,
        String subject,
        String description,
        String status,
        String priority,
        Long partnerId,
        Long reporterUserId,
        Long assigneeUserId,
        Long slaId,
        int slaResponseHours,
        int slaResolutionHours,
        Instant responseDeadline,
        Instant resolutionDeadline,
        boolean responseBreached,
        boolean resolutionBreached,
        Instant createdAt,
        Instant firstResponseAt,
        Instant resolvedAt,
        Instant closedAt,
        List<TicketAttachmentResponse> attachments
) {

    public static TicketResponse from(HelpdeskTicket ticket, Instant now) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                ticket.getPartnerId(),
                ticket.getReporterUserId(),
                ticket.getAssigneeUserId(),
                ticket.getSlaId(),
                ticket.getSlaResponseHours(),
                ticket.getSlaResolutionHours(),
                ticket.responseDeadline(),
                ticket.resolutionDeadline(),
                ticket.isResponseBreached(now),
                ticket.isResolutionBreached(now),
                ticket.getCreatedAt(),
                ticket.getFirstResponseAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.activeAttachments().stream().map(TicketAttachmentResponse::from).toList()
        );
    }
}
