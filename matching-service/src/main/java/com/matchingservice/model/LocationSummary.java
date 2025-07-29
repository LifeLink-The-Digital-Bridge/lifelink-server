package com.matchingservice.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class LocationSummary {
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
}
