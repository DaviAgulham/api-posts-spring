package com.example.api_spring.error;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
    var field = ex.getBindingResult().getFieldError();
    return ResponseEntity.badRequest().body(Map.of(
        "message", field != null ? field.getField()+" "+field.getDefaultMessage() : "validation error"
    ));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<?> notFound(EntityNotFoundException ex) {
    return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<?> forbidden(AccessDeniedException ex) {
    return ResponseEntity.status(403).body(Map.of("message","Forbidden"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
  }
}
