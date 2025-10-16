package com.recipientservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecipientNotFoundException.class)
    public ResponseEntity<?> handleRecipientNotFoundException(RecipientNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, "Recipient Not Found");
    }

    @ExceptionHandler(InvalidLocationException.class)
    public ResponseEntity<?> handleInvalidLocationException(InvalidLocationException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, "Invalid Location");
    }

    @ExceptionHandler(InvalidReceiveRequestException.class)
    public ResponseEntity<?> handleInvalidReceiveRequestException(InvalidReceiveRequestException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, "Invalid Receive Request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN, "Access Denied");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<?> buildResponse(String message, HttpStatus status, String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}

