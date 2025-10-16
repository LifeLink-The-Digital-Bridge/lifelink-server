package com.recipientservice.exceptions;

public class InvalidReceiveRequestException extends RuntimeException {
    public InvalidReceiveRequestException(String message) {
        super(message);
    }
}
