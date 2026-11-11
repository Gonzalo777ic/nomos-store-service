package com.valeerp.helpdesk;

public record TicketAttachmentRequest(
        String label,
        String url,
        String contentType,
        Long sizeBytes,
        String fileName
) {
}
