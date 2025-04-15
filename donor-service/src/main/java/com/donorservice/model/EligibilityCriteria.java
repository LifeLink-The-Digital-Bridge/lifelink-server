package com.donorservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "eligibility_criteria")
public class EligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "donor_id", referencedColumnName = "id")
    private Donor donor;

    @Column(nullable = false)
    private Boolean ageEligible;

    @Column(nullable = false)
    private Boolean weightEligible;

    @Column(nullable = false)
    private Boolean medicalClearance;

    @Column(nullable = false)
    private Boolean recentTattooOrPiercing;

    @Column(nullable = false)
    private Boolean recentTravel;
}
