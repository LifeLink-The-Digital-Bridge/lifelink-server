package com.matchingservice.model.recipients;

import com.matchingservice.enums.AlcoholStatus;
import com.matchingservice.enums.SmokingStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "recipient_eligibility_criteria")
public class RecipientEligibilityCriteria {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long eligibilityCriteriaId;

    @OneToOne
    @JoinColumn(name = "recipient_db_id", referencedColumnName = "id")
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

    @Column
    private Boolean isLivingDonor;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_status")
    private SmokingStatus smokingStatus;

    @Column(name = "pack_years")
    private Integer packYears;

    @Column(name = "quit_smoking_date")
    private LocalDate quitSmokingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "alcohol_status")
    private AlcoholStatus alcoholStatus;

    @Column(name = "drinks_per_week")
    private Integer drinksPerWeek;

    @Column(name = "quit_alcohol_date")
    private LocalDate quitAlcoholDate;

    @Column(name = "alcohol_abstinence_months")
    private Integer alcoholAbstinenceMonths;
}
