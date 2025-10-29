package com.matchingservice.enums;

import lombok.Getter;

@Getter
public enum MatchStatus {
    PENDING("Match created, awaiting confirmation"),
    DONOR_CONFIRMED("Donor confirmed, waiting for recipient"),
    RECIPIENT_CONFIRMED("Recipient confirmed, waiting for donor"),
    CONFIRMED("Both parties confirmed"),
    COMPLETED("Donation completed successfully"),
    WITHDRAWN("Withdrawn within grace period - can re-confirm"),
    REJECTED("Match rejected - cannot re-confirm"),
    EXPIRED("Match expired due to timeout"),
    CANCELLED_BY_DONOR("Cancelled - donor cancelled their donation"),
    CANCELLED_BY_RECIPIENT("Cancelled - recipient cancelled their request");

    private final String description;

    MatchStatus(String description) {
        this.description = description;
    }
}
