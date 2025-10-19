package com.matchingservice.enums;

public enum MatchStatus {
    PENDING("Match created, awaiting confirmation"),
    DONOR_CONFIRMED("Donor confirmed, waiting for recipient"),
    RECIPIENT_CONFIRMED("Recipient confirmed, waiting for donor"),
    CONFIRMED("Both parties confirmed"),
    EXPIRED("Match expired due to conflict"),
    CANCELLED_BY_DONOR("Cancelled - donor cancelled their donation"),
    CANCELLED_BY_RECIPIENT("Cancelled - recipient cancelled their request"),
    REJECTED("Match rejected by user"),
    FAILED("Match failed technical validation");

    private final String description;

    MatchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
