package com.matchingservice.dto.ml;

import com.matchingservice.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLRequestData {

    private UUID receiveRequestId;
    private UUID recipientId;
    private UUID userId;
    private UUID locationId;

    private RequestType requestType;
    private BloodType requestedBloodType;
    private OrganType requestedOrgan;
    private TissueType requestedTissue;
    private StemCellType requestedStemCellType;
    private UrgencyLevel urgencyLevel;
    private Double quantity;
    private LocalDate requestDate;
    private Integer daysWaiting;

    private Integer age;
    private LocalDate dob;
    private Double weight;
    private Double height;
    private Double bmi;
    private String bodySize;

    private Double hemoglobinLevel;
    private Double bloodGlucoseLevel;
    private Boolean hasDiabetes;
    private String bloodPressure;
    private Boolean hasInfectiousDiseases;
    private String infectiousDiseaseDetails;
    private Double creatinineLevel;
    private String liverFunctionTests;
    private String cardiacStatus;
    private Double pulmonaryFunction;
    private String overallHealthStatus;
    private String diagnosis;
    private String allergies;

    private SmokingStatus smokingStatus;
    private Integer packYears;
    private AlcoholStatus alcoholStatus;
    private Integer drinksPerWeek;

    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    private String state;
    private String country;

    private String hlaA1;
    private String hlaA2;
    private String hlaB1;
    private String hlaB2;
    private String hlaC1;
    private String hlaC2;
    private String hlaDR1;
    private String hlaDR2;
    private String hlaDQ1;
    private String hlaDQ2;
    private String hlaDP1;
    private String hlaDP2;
    private Boolean hlaHighResolution;
    private String hlaString;
}
