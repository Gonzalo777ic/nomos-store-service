package com.valeerp.kernel;

import com.valeerp.kernel.api.DomainErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DomainExceptionHandler {

    public static final String NOT_FOUND_CODE = "NOT_FOUND";

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<DomainErrorBody> handle(DomainException exception) {
        HttpStatus status = NOT_FOUND_CODE.equals(exception.code())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new DomainErrorBody(exception.code(), exception.getMessage()));
    }
}
