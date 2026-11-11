package com.valeerp.helpdesk;

public record TicketCreateRequest(
        String subject,
        String description,
        String priority,
        Long partnerId,
        Long reporterUserId,
        Long assigneeUserId,
        Long slaId
) {
}
