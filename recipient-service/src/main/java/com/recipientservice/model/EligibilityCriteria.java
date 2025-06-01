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
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;

    @Column(nullable = false)
    private Boolean medicallyEligible;

    @Column(nullable = false)
    private Boolean legalClearance;

    @Column
    private String notes;

    @Column
    private LocalDate lastReviewed;
}
