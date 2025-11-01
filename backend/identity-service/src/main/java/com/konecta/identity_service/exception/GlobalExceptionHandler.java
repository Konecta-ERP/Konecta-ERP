package com.konecta.identity_service.exception;

import com.konecta.identity_service.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseApiException(BaseApiException ex) {
        HttpStatus status;
        if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof DuplicateResourceException) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        ApiResponse<?> response = ApiResponse.error(
                status.value(),
                ex.getMessage(),
                ex.getClientMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse<?> response = ApiResponse.error(401,
                "Authentication Failed: " + ex.getMessage(),
                "Invalid email or password.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 403 Forbidden (for @PreAuthorize failures)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse<?> response = ApiResponse.error(403,
                "User Role Not Permitted: " + ex.getMessage(),
                "Access denied.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 400 Bad Request (for @Valid validation failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponse<?> response = ApiResponse.error(400, errors.toString(), "Validation failed.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 500 Internal Server Error (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        ex.printStackTrace();
        ApiResponse<?> response = ApiResponse.error(500, ex.getMessage(), "An unexpected server error occurred.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}