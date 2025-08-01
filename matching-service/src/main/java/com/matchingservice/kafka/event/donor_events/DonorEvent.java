package com.matchingservice.kafka.event.donor_events;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DonorEvent {
    private UUID donorId;
    private UUID userId;
    private LocalDate registrationDate;
    private String status;
    private Double weight;
    private Integer age;
    private Boolean medicalClearance;
    private Boolean recentSurgery;
    private String chronicDiseases;
    private String allergies;
    private LocalDate lastDonationDate;
    private Double hemoglobinLevel;
    private String bloodPressure;
    private Boolean hasDiseases;
    private Boolean takingMedication;
    private String diseaseDescription;
}
