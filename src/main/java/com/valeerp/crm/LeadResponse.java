package com.valeerp.crm;

import java.time.Instant;

public record LeadResponse(
        Long id,
        String title,
        String contactName,
        String email,
        String phone,
        Long partnerId,
        String source,
        String notes,
        String status,
        String disqualifyReason,
        Long convertedOpportunityId,
        Instant createdAt
) {

    public static LeadResponse from(CrmLead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getTitle(),
                lead.getContactName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getPartnerId(),
                lead.getSource(),
                lead.getNotes(),
                lead.getStatus().name(),
                lead.getDisqualifyReason(),
                lead.getConvertedOpportunityId(),
                lead.getCreatedAt()
        );
    }
}
