package com.valeerp.kernel.api;

/**
 * Cuerpo homogéneo de error de dominio para respuestas HTTP.
 */
public record DomainErrorBody(String code, String message) {
}
