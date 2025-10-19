package com.donorservice.enums;

import lombok.Getter;

@Getter
public enum DonationStatus {
    PENDING("Donation registered, awaiting matching"),
    AVAILABLE("Ready for matching"),
    MATCHED("Has active matches"),
    IN_PROGRESS("Confirmed match, donation process started"),
    COMPLETED("Donation successfully completed"),
    CANCELLED_BY_DONOR("Cancelled by donor"),
    CANCELLED_DUE_TO_MATCH_FAILURE("Cancelled due to match failure"),
    EXPIRED("Donation expired"),
    WITHDRAWN("Withdrawn after confirmation");

    private final String description;

    DonationStatus(String description) {
        this.description = description;
    }

}
