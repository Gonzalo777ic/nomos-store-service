package com.valeerp.kernel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handle(DomainException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
