package com.matchingservice.model;

import com.matchingservice.enums.DonorStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "donors")
@Data
public class Donor {

    @Id
    private UUID donorId;

    private UUID userId;

    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    private DonorStatus status;

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
    private Boolean recentlyIll;


    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Donation> donations = new ArrayList<>();

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();
}

