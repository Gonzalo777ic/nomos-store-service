package com.valeerp.pos;

public record PosTicketOpenRequest(
        Long posConfigId,
        Long cashSessionId,
        Long partnerId
) {
}
