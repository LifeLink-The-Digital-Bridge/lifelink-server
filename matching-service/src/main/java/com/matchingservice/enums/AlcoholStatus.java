package com.matchingservice.enums;

public enum AlcoholStatus {
    NO_ALCOHOL_USE("No alcohol use"),
    MODERATE_USE("Moderate use"),
    HEAVY_USE("Heavy use"),
    FORMER_USER("Former user");

    private final String description;

    AlcoholStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
