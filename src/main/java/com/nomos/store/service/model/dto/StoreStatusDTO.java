package com.nomos.store.service.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoreStatusDTO {

    private String status;

    private String message;

    private String reason;

    private LocalDateTime nextOpening;
    private LocalDateTime currentClosing;

    private boolean closingSoon;
}