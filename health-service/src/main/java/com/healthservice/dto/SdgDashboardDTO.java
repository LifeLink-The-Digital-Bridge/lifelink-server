package com.healthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdgDashboardDTO {
    private long totalUsers;
    private long totalMigrants;
    private long totalDoctors;
    private long totalNGOs;
    private long totalHealthIds;
    private long totalHealthRecords;
    private long emergencyRecords;
    private long activeDoctorPatientConnections;
    private long activeNgoMigrantConnections;
    private long newUsersThisMonth;
    private long newHealthRecordsThisMonth;
    private double migrantHealthIdCoveragePercent;
    private Map<String, Long> stateDistribution;
    private Map<String, Double> sdgScorecard;
}
