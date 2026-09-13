package com.dung.personal_finance_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                request,
                "Email or password is incorrect",
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(request, "Access is denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            AccountNotFoundException.class,
            CategoryNotFoundException.class,
            FinancialTransactionNotFoundException.class,
            BudgetNotFoundException.class
    })

    public ResponseEntity<Object> handleNotFoundException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("message", exception.getMessage());
        response.put("httpStatus", HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRuleException(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("message", exception.getMessage());
        response.put("httpStatus", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("message", "Validation failed");
        response.put("httpStatus", HttpStatus.BAD_REQUEST);
        response.put("data", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected exception at {}",
                request.getRequestURI(),
                exception
        );

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("message", "An unexpected error occurred");
        response.put("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put(
                "message",
                "Invalid value for parameter: " + exception.getName()
        );
        response.put("httpStatus", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put(
                "message",
                "Request conflicts with existing data"
        );
        response.put("httpStatus", HttpStatus.CONFLICT);

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<Object> handleDateTimeException(
            DateTimeException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("message", "Invalid date");
        response.put("httpStatus", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }

    private ResponseEntity<Object> buildErrorResponse(
            HttpServletRequest request,
            String message,
            HttpStatus status
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("path", request.getRequestURI());
        response.put("message", message);
        response.put("httpStatus", status);
        return new ResponseEntity<>(response, status);
    }
}
