package com.konecta.recruitmentservice.exception;

import com.konecta.recruitmentservice.dto.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

@ControllerAdvice
public class GlobalExceptionHandler {

  // Handles 404 Not Found
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(EntityNotFoundException ex) {
    int status = HttpStatus.NOT_FOUND.value();
    ApiResponse<Object> response = ApiResponse.error(
        status,
        ex.getMessage(),
        "The resource you requested could not be found.");
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  // Handles 400 Bad Request
  @ExceptionHandler({ IllegalStateException.class, IllegalArgumentException.class })
  public ResponseEntity<ApiResponse<Object>> handleIllegalState(Exception ex) {
    int status = HttpStatus.BAD_REQUEST.value();
    ApiResponse<Object> response = ApiResponse.error(
        status,
        ex.getMessage(),
        "Your request could not be processed as submitted.");
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Handles 403 Forbidden
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
    int status = HttpStatus.FORBIDDEN.value();
    ApiResponse<Object> response = ApiResponse.error(
        status,
        ex.getMessage(),
        "You do not have permission to perform this action.");
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  // Handles 401 Unauthorized (authentication failures)
  @ExceptionHandler({ AuthenticationException.class, InsufficientAuthenticationException.class })
  public ResponseEntity<ApiResponse<Object>> handleAuthenticationFailure(Exception ex) {
    int status = HttpStatus.UNAUTHORIZED.value();
    ApiResponse<Object> response = ApiResponse.error(
        status,
        ex.getMessage(),
        "Authentication is required to access this resource.");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  // Handles 500 Internal Server Error
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
    int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
    ApiResponse<Object> response = ApiResponse.error(
        status,
        ex.getMessage(),
        "An unexpected error occurred. Please try again later.");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // Handles validation errors for @Valid annotated request bodies
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    int status = HttpStatus.BAD_REQUEST.value();

    // Collect all validation errors
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    ApiResponse<Object> response = ApiResponse.error(
        status,
        errors.toString(),
        "Validation failed. Please check your data.");

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }
}