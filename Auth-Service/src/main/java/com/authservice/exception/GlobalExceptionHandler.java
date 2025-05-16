package com.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(ex.getMessage()));
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(TokenInvalidException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(ex.getMessage()));
    }

    @ExceptionHandler(InvalidOTPException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOTP(InvalidOTPException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        String message = sb.toString().trim();
        if (message.endsWith(";")) {
            message = message.substring(0, message.length() - 1);
        }
        return ResponseEntity.badRequest().body(buildError(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError("Internal error: " + ex.getMessage()));
    }

    private Map<String, String> buildError(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return error;
    }
}
