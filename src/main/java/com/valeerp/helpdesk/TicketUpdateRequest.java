package com.valeerp.helpdesk;

public record TicketUpdateRequest(
        String subject,
        String description,
        String priority,
        Long partnerId,
        Long assigneeUserId,
        Long slaId
) {
}
