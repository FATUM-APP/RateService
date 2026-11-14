package fatum.rate.controller;

import fatum.rate.exception.CalificationErrorCode;
import fatum.rate.exception.FatumServiceException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns every failure into the same body: {@code code} and {@code message}.
 *
 * <p>Same contract as CatalogService, so the GUI reads one shape from every service. The code is
 * what a client branches on; the message is for the human reading the log.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FatumServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(FatumServiceException exception) {
        CalificationErrorCode code = exception.getErrorCode();
        log.warn("Refused with {}: {}", code, exception.getMessage());
        return body(code.getHttpStatus(), code.name(), exception.getMessage());
    }

    /** A body that does not pass validation is a 400, not a 500. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return validationBody(fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fields.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return validationBody(fields);
    }

    /**
     * Two ratings of the same order arriving at the same time.
     *
     * <p>The unique key or the version of the copy rejects the second one; the caller is told to try
     * again, which will then answer the real reason — the order is already rated.</p>
     */
    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(Exception exception) {
        log.warn("Concurrent write refused", exception);
        return body(
                CalificationErrorCode.CONCURRENT_UPDATE.getHttpStatus(),
                CalificationErrorCode.CONCURRENT_UPDATE.name(),
                CalificationErrorCode.CONCURRENT_UPDATE.getDefaultMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException exception) {
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
        log.error("Unexpected failure", exception);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> validationBody(Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", CalificationErrorCode.VALIDATION_FAILED.name());
        body.put("message", CalificationErrorCode.VALIDATION_FAILED.getDefaultMessage());
        body.put("fields", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatusCode status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
