package com.recipientservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "recipient_eligibility_criteria")
public class EligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", unique = true)
    private Recipient recipient;

    @Column(nullable = false)
    private Boolean ageEligible;

    @Column
    private Integer age;

    @Column
    private LocalDate dob;

    @Column(nullable = false)
    private Boolean weightEligible;

    @Column
    private Double weight;

    @Column(nullable = false)
    private Boolean medicallyEligible;

    @Column(nullable = false)
    private Boolean legalClearance;

    @Column
    private String notes;

    @Column
    private LocalDate lastReviewed;

    @Column
    private Double height;

    @Column
    private Double bodyMassIndex;

    @Column
    private String bodySize;

    @Column(nullable = false)
    private Boolean isLivingDonor;

}
