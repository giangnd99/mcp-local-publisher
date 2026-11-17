package com.gth.posting.api.exception;

import com.gth.posting.domain.exception.InvalidStateTransitionException;
import com.gth.posting.domain.exception.PostingDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Dich loi nghiep vu sang HTTP. Domain khong biet HTTP ton tai,
 * day la noi duy nhat biet.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition(InvalidStateTransitionException ex) {
        return build(HttpStatus.CONFLICT, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(PostingDomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomain(PostingDomainException ex) {
        HttpStatus status = "CAMPAIGN_NOT_FOUND".equals(ex.code()) || "JOB_NOT_FOUND".equals(ex.code())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return build(status, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", code,
                "message", message == null ? "" : message
        ));
    }
}
