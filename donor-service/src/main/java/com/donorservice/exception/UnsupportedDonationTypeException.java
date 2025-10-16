package com.donorservice.exception;

public class UnsupportedDonationTypeException extends RuntimeException {
    public UnsupportedDonationTypeException(String message) {
        super(message);
    }
}
