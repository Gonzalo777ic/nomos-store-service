package com.valeerp.helpdesk;

import java.time.Instant;

public record TicketAttachmentResponse(
        Long id,
        Long ticketId,
        String label,
        String url,
        String contentType,
        Long sizeBytes,
        String fileName,
        Instant createdAt
) {

    public static TicketAttachmentResponse from(TicketAttachment attachment) {
        return new TicketAttachmentResponse(
                attachment.getId(),
                attachment.getTicketId(),
                attachment.getLabel(),
                attachment.getUrl(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getFileName(),
                attachment.getCreatedAt()
        );
    }
}
