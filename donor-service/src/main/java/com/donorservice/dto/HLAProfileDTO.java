package com.donorservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HLAProfileDTO {
    private Long id;
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
    private LocalDate testingDate;
    private String testingMethod;
    private String laboratoryName;
    private String certificationNumber;
    private String hlaString;
    private Boolean isHighResolution;
}

