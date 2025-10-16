package com.recipientservice.exceptions;

public class IncompleteProfileException extends RuntimeException {
    public IncompleteProfileException(String message) {
        super(message);
    }
}
