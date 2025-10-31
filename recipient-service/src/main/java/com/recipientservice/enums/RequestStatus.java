package com.recipientservice.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING("Request created, ready for matching"),
    MATCHED("Has active matches waiting for confirmation"),
    IN_PROGRESS("Confirmed match, receiving process started"),
    FULFILLED("Request successfully fulfilled"),
    CANCELLED_BY_RECIPIENT("Cancelled by recipient"),
    CANCELLED_DUE_TO_MATCH_FAILURE("Cancelled due to match failure");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }
}
