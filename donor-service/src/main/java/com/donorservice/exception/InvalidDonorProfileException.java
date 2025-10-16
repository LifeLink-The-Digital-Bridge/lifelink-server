package com.donorservice.exception;

public class InvalidDonorProfileException extends RuntimeException {
    public InvalidDonorProfileException(String message) {
        super(message);
    }
}
