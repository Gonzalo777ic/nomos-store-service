package com.valeerp.pos;

import java.util.List;

public record PosReturnCreateRequest(
        Long ticketId,
        String reason,
        List<Line> lines
) {
    public record Line(Long ticketLineId, int quantity) {
    }
}
