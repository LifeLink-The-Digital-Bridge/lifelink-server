package com.matchingservice.model;

import com.matchingservice.enums.OrganType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "organ_donations")
public class OrganDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganType organType;

    @Column(nullable = false)
    private Boolean isCompatible;

    @Column
    private String organQuality;

    @Column
    private LocalDateTime organViabilityExpiry;

    @Column
    private Integer coldIschemiaTime;

    @Column
    private Boolean organPerfused;

    @Column
    private Double organWeight;

    @Column
    private String organSize;

    @Column
    private String functionalAssessment;

    @Column
    private Boolean hasAbnormalities;

    @Column
    private String abnormalityDescription;
}
