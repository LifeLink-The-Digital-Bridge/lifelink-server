package com.matchingservice.model.donor;

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
    @Column(name = "organ_type")
    private OrganType organType;

    @Column(name = "is_compatible")
    private Boolean isCompatible;

    @Column(name = "organ_quality")
    private String organQuality;

    @Column(name = "organ_viability_expiry")
    private LocalDateTime organViabilityExpiry;

    @Column(name = "cold_ischemia_time")
    private Integer coldIschemiaTime;

    @Column(name = "organ_perfused")
    private Boolean organPerfused;

    @Column(name = "organ_weight")
    private Double organWeight;

    @Column(name = "organ_size")
    private String organSize;

    @Column(name = "functional_assessment")
    private String functionalAssessment;

    @Column(name = "has_abnormalities")
    private Boolean hasAbnormalities;

    @Column(name = "abnormality_description")
    private String abnormalityDescription;
}
