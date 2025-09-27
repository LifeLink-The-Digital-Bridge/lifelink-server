package com.matchingservice.enums;

public enum MatchStatus {
    PENDING("Match created, awaiting confirmation"),
    CONFIRMED("Both parties confirmed"),
    EXPIRED("Match expired due to conflict"),
    CANCELLED("Match cancelled by user"),
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
