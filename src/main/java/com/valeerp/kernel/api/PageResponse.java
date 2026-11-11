package com.valeerp.kernel.api;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envoltorio de paginación para respuestas de API (nunca Page de Spring crudo).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
