package com.donorservice.enums;

import lombok.Getter;

@Getter
public enum DonationStatus {
    PENDING("Donation registered, ready for matching"),
    MATCHED("Has active matches waiting for confirmation"),
    IN_PROGRESS("Confirmed match, donation process started"),
    COMPLETED("Donation successfully completed"),
    CANCELLED_BY_DONOR("Cancelled by donor"),
    CANCELLED_DUE_TO_MATCH_FAILURE("Cancelled due to match failure");

    private final String description;

    DonationStatus(String description) {
        this.description = description;
    }
}

