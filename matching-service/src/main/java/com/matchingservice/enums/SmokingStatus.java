package com.matchingservice.enums;

public enum SmokingStatus {
    NEVER_SMOKED("Never smoked"),
    FORMER_SMOKER("Former smoker"),
    CURRENT_SMOKER("Current smoker"),
    OCCASIONAL_SMOKER("Occasional smoker");

    private final String description;

    SmokingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

