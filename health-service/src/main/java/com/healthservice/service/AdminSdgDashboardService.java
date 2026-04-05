package com.healthservice.service;

import com.healthservice.dto.SdgDashboardDTO;
import com.healthservice.dto.UserAnalyticsDTO;
import com.healthservice.feign.UserServiceClient;
import com.healthservice.model.HealthID;
import com.healthservice.repository.DoctorPatientAssociationRepository;
import com.healthservice.repository.HealthIDRepository;
import com.healthservice.repository.HealthRecordRepository;
import com.healthservice.repository.NGOMigrantAssociationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSdgDashboardService {

    private final UserServiceClient userServiceClient;
    private final HealthIDRepository healthIDRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final DoctorPatientAssociationRepository doctorPatientAssociationRepository;
    private final NGOMigrantAssociationRepository ngoMigrantAssociationRepository;

    public SdgDashboardDTO getDashboard(String authorizationToken, String rolesHeader) {
        UserAnalyticsDTO userAnalytics = userServiceClient.getAdminAnalytics(authorizationToken, rolesHeader);

        long totalHealthIds = healthIDRepository.count();
        long totalHealthRecords = healthRecordRepository.count();
        long emergencyRecords = healthRecordRepository.countByIsEmergencyTrue();
        long activeDoctorPatientConnections = doctorPatientAssociationRepository.countByIsActiveTrue();
        long activeNgoMigrantConnections = ngoMigrantAssociationRepository.countByIsActiveTrue();
        long newHealthRecordsThisMonth = healthRecordRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30));

        double migrantCoverage = userAnalytics.getTotalMigrants() > 0
                ? round2((double) totalHealthIds * 100.0 / (double) userAnalytics.getTotalMigrants())
                : 0.0;

        Map<String, Long> stateDistribution = healthIDRepository.findAll().stream()
                .map(HealthID::getCurrentState)
                .filter(state -> state != null && !state.isBlank())
                .collect(Collectors.groupingBy(state -> state, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Double> sdgScorecard = new LinkedHashMap<>();
        sdgScorecard.put("sdg3_health_coverage", migrantCoverage);
        sdgScorecard.put("sdg10_inclusion_connections", scoreFromConnections(activeDoctorPatientConnections, activeNgoMigrantConnections, userAnalytics.getTotalMigrants()));
        sdgScorecard.put("sdg11_state_reach", scoreFromStateReach(stateDistribution.size()));
        sdgScorecard.put("sdg17_partnership_density", scoreFromPartnershipDensity(activeDoctorPatientConnections, activeNgoMigrantConnections, userAnalytics.getTotalUsers()));

        return new SdgDashboardDTO(
                userAnalytics.getTotalUsers(),
                userAnalytics.getTotalMigrants(),
                userAnalytics.getTotalDoctors(),
                userAnalytics.getTotalNGOs(),
                totalHealthIds,
                totalHealthRecords,
                emergencyRecords,
                activeDoctorPatientConnections,
                activeNgoMigrantConnections,
                userAnalytics.getNewUsersThisMonth(),
                newHealthRecordsThisMonth,
                migrantCoverage,
                stateDistribution,
                sdgScorecard
        );
    }

    private double scoreFromConnections(long doctorConnections, long ngoConnections, long totalMigrants) {
        if (totalMigrants <= 0) {
            return 0.0;
        }
        double raw = (double) (doctorConnections + ngoConnections) * 100.0 / (double) totalMigrants;
        return round2(Math.min(raw, 100.0));
    }

    private double scoreFromStateReach(int statesCovered) {
        double raw = (double) statesCovered * 100.0 / 14.0;
        return round2(Math.min(raw, 100.0));
    }

    private double scoreFromPartnershipDensity(long doctorConnections, long ngoConnections, long totalUsers) {
        if (totalUsers <= 0) {
            return 0.0;
        }
        double raw = (double) (doctorConnections + ngoConnections) * 100.0 / (double) totalUsers;
        return round2(Math.min(raw * 4.0, 100.0));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
