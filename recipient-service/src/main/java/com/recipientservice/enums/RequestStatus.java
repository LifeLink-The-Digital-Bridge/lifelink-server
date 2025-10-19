package com.recipientservice.enums;

public enum RequestStatus {
    PENDING("Request created, awaiting matching"),
    ACTIVE("Ready for matching"),
    MATCHED("Has active matches"),
    IN_PROGRESS("Confirmed match, receiving process started"),
    FULFILLED("Request successfully fulfilled"),
    CANCELLED_BY_RECIPIENT("Cancelled by recipient"),
    CANCELLED_DUE_TO_MATCH_FAILURE("Cancelled due to match failure"),
    EXPIRED("Request expired"),
    WITHDRAWN("Withdrawn after confirmation");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
