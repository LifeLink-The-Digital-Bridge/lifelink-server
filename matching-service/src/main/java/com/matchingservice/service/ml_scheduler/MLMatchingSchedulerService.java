package com.matchingservice.service.ml_scheduler;

import com.matchingservice.client.MLMatchingClient;
import com.matchingservice.dto.ml.*;
import com.matchingservice.enums.*;
import com.matchingservice.model.MatchResult;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.*;
import com.matchingservice.repository.*;
import com.matchingservice.repository.donor.*;
import com.matchingservice.repository.recipient.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLMatchingSchedulerService {

    private final DonationRepository donationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final MatchResultRepository matchResultRepository;
    private final DonorHLAProfileRepository donorHLAProfileRepository;
    private final RecipientHLAProfileRepository recipientHLAProfileRepository;
    private final MLMatchingClient mlMatchingClient;

    @Value("${ml.service.enabled:true}")
    private boolean mlServiceEnabled;

    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void runBatchMatching() {
        log.info("Starting scheduled batch matching at {}", LocalDateTime.now());

        if (!mlServiceEnabled) {
            log.warn("ML service is disabled. Skipping batch matching.");
            return;
        }

        try {
            List<ReceiveRequest> pendingRequests = receiveRequestRepository
                    .findByStatus(RequestStatus.PENDING);

            log.info("Found {} pending receive requests", pendingRequests.size());

            if (pendingRequests.isEmpty()) {
                log.info("No pending requests. Skipping batch matching.");
                return;
            }

            List<Donation> availableDonations = donationRepository
                    .findByStatus(DonationStatus.PENDING);

            log.info("Found {} available donations", availableDonations.size());

            if (availableDonations.isEmpty()) {
                log.info("No available donations. Skipping batch matching.");
                return;
            }

            Map<RequestType, List<ReceiveRequest>> requestsByType = pendingRequests.stream()
                    .collect(Collectors.groupingBy(ReceiveRequest::getRequestType));

            Map<DonationType, List<Donation>> donationsByType = availableDonations.stream()
                    .collect(Collectors.groupingBy(Donation::getDonationType));

            int totalMatchesCreated = 0;

            for (RequestType requestType : requestsByType.keySet()) {
                DonationType donationType = DonationType.valueOf(requestType.name());

                List<ReceiveRequest> typeRequests = requestsByType.get(requestType);
                List<Donation> typeDonations = donationsByType.getOrDefault(donationType, List.of());

                if (typeDonations.isEmpty()) {
                    log.info("No donations available for type: {}", requestType);
                    continue;
                }

                log.info("Matching {} requests with {} donations for type: {}",
                        typeRequests.size(), typeDonations.size(), requestType);

                int matchesForType = performMLMatching(typeRequests, typeDonations);
                totalMatchesCreated += matchesForType;

                log.info("Created {} matches for type: {}", matchesForType, requestType);
            }

            log.info("Batch matching completed. Total matches created: {}", totalMatchesCreated);

        } catch (Exception e) {
            log.error("Error during batch matching: {}", e.getMessage(), e);
        }
    }

    private int performMLMatching(List<ReceiveRequest> requests, List<Donation> donations) {
        try {
            log.info("Calling ML service via Eureka...");

            List<MLRequestData> mlRequests = requests.stream()
                    .map(this::convertToMLRequestData)
                    .collect(Collectors.toList());

            List<MLDonationData> mlDonations = donations.stream()
                    .map(this::convertToMLDonationData)
                    .collect(Collectors.toList());

            MLBatchMatchRequest mlRequest = MLBatchMatchRequest.builder()
                    .requests(mlRequests)
                    .donations(mlDonations)
                    .topN(10)
                    .threshold(0.5)
                    .build();

            long startTime = System.currentTimeMillis();
            MLBatchMatchResponse mlResponse = mlMatchingClient.batchMatch(mlRequest);
            long endTime = System.currentTimeMillis();

            log.info("ML service responded in {}ms", endTime - startTime);
            log.info("ML found {} matches", mlResponse.getMatchesFound());
            log.info("ML model version: {}", mlResponse.getModelVersion());

            if (!mlResponse.getSuccess()) {
                log.error("ML service returned error: {}", mlResponse.getError());
                return 0;
            }

            return processMLMatches(mlResponse.getMatches());

        } catch (Exception e) {
            log.error("ML service error: {}", e.getMessage());
            return 0;
        }
    }

    private int processMLMatches(List<MLMatchResult> mlMatches) {
        int matchCount = 0;

        for (MLMatchResult mlMatch : mlMatches) {
            try {
                boolean exists = matchResultRepository
                        .existsByDonationIdAndReceiveRequestId(
                                mlMatch.getDonationId(),
                                mlMatch.getReceiveRequestId()
                        );

                if (exists) {
                    log.debug("Match already exists, skipping");
                    continue;
                }

                MatchResult matchResult = new MatchResult();
                matchResult.setDonationId(mlMatch.getDonationId());
                matchResult.setReceiveRequestId(mlMatch.getReceiveRequestId());
                matchResult.setDonorUserId(mlMatch.getDonorUserId());
                matchResult.setRecipientUserId(mlMatch.getRecipientUserId());
                matchResult.setDonorLocationId(mlMatch.getDonorLocationId());
                matchResult.setRecipientLocationId(mlMatch.getRecipientLocationId());

                matchResult.setCompatibilityScore(mlMatch.getCompatibilityScore());
                matchResult.setBloodCompatibilityScore(mlMatch.getBloodCompatibilityScore());
                matchResult.setLocationCompatibilityScore(mlMatch.getLocationCompatibilityScore());
                matchResult.setMedicalCompatibilityScore(mlMatch.getMedicalCompatibilityScore());
                matchResult.setUrgencyPriorityScore(mlMatch.getUrgencyPriorityScore());
                matchResult.setDistance(mlMatch.getDistanceKm());
                matchResult.setMatchReason(mlMatch.getMatchReason());
                matchResult.setPriorityRank(mlMatch.getPriorityRank());

                matchResult.setStatus(MatchStatus.PENDING);
                matchResult.setMatchedAt(LocalDateTime.now());
                matchResult.setIsConfirmed(false);
                matchResult.setDonorConfirmed(false);
                matchResult.setRecipientConfirmed(false);

                matchResultRepository.save(matchResult);
                matchCount++;

                updateDonationStatus(mlMatch.getDonationId(), DonationStatus.MATCHED);
                updateRequestStatus(mlMatch.getReceiveRequestId(), RequestStatus.MATCHED);

                log.debug("Created ML match (score: {}, rank: {})",
                        matchResult.getCompatibilityScore(),
                        matchResult.getPriorityRank()
                );

            } catch (Exception e) {
                log.error("Error processing ML match: {}", e.getMessage());
            }
        }

        return matchCount;
    }

    private void updateDonationStatus(UUID donationId, DonationStatus newStatus) {
        donationRepository.findById(donationId).ifPresent(donation -> {
            donation.setStatus(newStatus);
            donationRepository.save(donation);
        });
    }

    private void updateRequestStatus(UUID requestId, RequestStatus newStatus) {
        receiveRequestRepository.findById(requestId).ifPresent(request -> {
            request.setStatus(newStatus);
            receiveRequestRepository.save(request);
        });
    }

    private MLRequestData convertToMLRequestData(ReceiveRequest request) {
        Recipient recipient = request.getRecipient();
        RecipientMedicalDetails medical = recipient != null ? recipient.getMedicalDetails() : null;
        RecipientEligibilityCriteria eligibility = recipient != null ? recipient.getEligibilityCriteria() : null;
        RecipientLocation location = request.getLocation();

        int daysWaiting = (int) ChronoUnit.DAYS.between(request.getRequestDate(), LocalDate.now());

        String hlaA1 = null, hlaA2 = null, hlaB1 = null, hlaB2 = null, hlaC1 = null, hlaC2 = null;
        String hlaDR1 = null, hlaDR2 = null, hlaDQ1 = null, hlaDQ2 = null, hlaDP1 = null, hlaDP2 = null;
        String hlaString = null;
        Boolean hlaHighRes = null;

        if (request.getRequestType() == RequestType.ORGAN || request.getRequestType() == RequestType.STEM_CELL) {
            RecipientHLAProfile hlaProfile = recipientHLAProfileRepository
                    .findTopByRecipient_RecipientIdOrderByEventTimestampDesc(request.getRecipientId())
                    .orElse(null);

            if (hlaProfile != null) {
                hlaA1 = hlaProfile.getHlaA1();
                hlaA2 = hlaProfile.getHlaA2();
                hlaB1 = hlaProfile.getHlaB1();
                hlaB2 = hlaProfile.getHlaB2();
                hlaC1 = hlaProfile.getHlaC1();
                hlaC2 = hlaProfile.getHlaC2();
                hlaDR1 = hlaProfile.getHlaDR1();
                hlaDR2 = hlaProfile.getHlaDR2();
                hlaDQ1 = hlaProfile.getHlaDQ1();
                hlaDQ2 = hlaProfile.getHlaDQ2();
                hlaDP1 = hlaProfile.getHlaDP1();
                hlaDP2 = hlaProfile.getHlaDP2();
                hlaString = hlaProfile.getHlaString();
                hlaHighRes = hlaProfile.getIsHighResolution();
            }
        }

        return MLRequestData.builder()
                .receiveRequestId(request.getReceiveRequestId())
                .recipientId(request.getRecipientId())
                .userId(recipient != null ? recipient.getUserId() : null)
                .locationId(location != null ? location.getLocationId() : null)
                .requestType(request.getRequestType())
                .requestedBloodType(request.getRequestedBloodType())
                .requestedOrgan(request.getRequestedOrgan())
                .requestedTissue(request.getRequestedTissue())
                .requestedStemCellType(request.getRequestedStemCellType())
                .urgencyLevel(request.getUrgencyLevel())
                .quantity(request.getQuantity())
                .requestDate(request.getRequestDate())
                .daysWaiting(daysWaiting)
                .age(eligibility != null ? eligibility.getAge() : null)
                .dob(eligibility != null ? eligibility.getDob() : null)
                .weight(eligibility != null ? eligibility.getWeight() : null)
                .height(eligibility != null ? eligibility.getHeight() : null)
                .bmi(eligibility != null ? eligibility.getBodyMassIndex() : null)
                .bodySize(eligibility != null ? eligibility.getBodySize() : null)
                .hemoglobinLevel(medical != null ? medical.getHemoglobinLevel() : null)
                .bloodGlucoseLevel(medical != null ? medical.getBloodGlucoseLevel() : null)
                .hasDiabetes(medical != null ? medical.getHasDiabetes() : false)
                .bloodPressure(medical != null ? medical.getBloodPressure() : null)
                .hasInfectiousDiseases(medical != null ? medical.getHasInfectiousDiseases() : false)
                .infectiousDiseaseDetails(medical != null ? medical.getInfectiousDiseaseDetails() : null)
                .creatinineLevel(medical != null ? medical.getCreatinineLevel() : null)
                .liverFunctionTests(medical != null ? medical.getLiverFunctionTests() : null)
                .cardiacStatus(medical != null ? medical.getCardiacStatus() : null)
                .pulmonaryFunction(medical != null ? medical.getPulmonaryFunction() : null)
                .overallHealthStatus(medical != null ? medical.getOverallHealthStatus() : null)
                .diagnosis(medical != null ? medical.getDiagnosis() : null)
                .allergies(medical != null ? medical.getAllergies() : null)
                .smokingStatus(eligibility != null ? eligibility.getSmokingStatus() : null)
                .packYears(eligibility != null ? eligibility.getPackYears() : null)
                .alcoholStatus(eligibility != null ? eligibility.getAlcoholStatus() : null)
                .drinksPerWeek(eligibility != null ? eligibility.getDrinksPerWeek() : null)
                .latitude(location != null ? location.getLatitude() : null)
                .longitude(location != null ? location.getLongitude() : null)
                .city(location != null ? location.getCity() : null)
                .district(location != null ? location.getDistrict() : null)
                .state(location != null ? location.getState() : null)
                .country(location != null ? location.getCountry() : null)
                .hlaA1(hlaA1)
                .hlaA2(hlaA2)
                .hlaB1(hlaB1)
                .hlaB2(hlaB2)
                .hlaC1(hlaC1)
                .hlaC2(hlaC2)
                .hlaDR1(hlaDR1)
                .hlaDR2(hlaDR2)
                .hlaDQ1(hlaDQ1)
                .hlaDQ2(hlaDQ2)
                .hlaDP1(hlaDP1)
                .hlaDP2(hlaDP2)
                .hlaHighResolution(hlaHighRes)
                .hlaString(hlaString)
                .build();
    }

    private MLDonationData convertToMLDonationData(Donation donation) {
        Donor donor = donation.getDonor();
        DonorMedicalDetails medical = donor != null ? donor.getMedicalDetails() : null;
        DonorEligibilityCriteria eligibility = donor != null ? donor.getEligibilityCriteria() : null;
        DonorLocation location = donation.getLocation();

        Integer daysSinceLastDonation = null;
        if (eligibility != null && eligibility.getLastDonationDate() != null) {
            daysSinceLastDonation = (int) ChronoUnit.DAYS.between(
                    eligibility.getLastDonationDate(), LocalDate.now()
            );
        }

        String hlaA1 = null, hlaA2 = null, hlaB1 = null, hlaB2 = null, hlaC1 = null, hlaC2 = null;
        String hlaDR1 = null, hlaDR2 = null, hlaDQ1 = null, hlaDQ2 = null, hlaDP1 = null, hlaDP2 = null;
        String hlaString = null;
        Boolean hlaHighRes = null;

        if (donation.getDonationType() == DonationType.ORGAN || donation.getDonationType() == DonationType.STEM_CELL) {
            DonorHLAProfile hlaProfile = donorHLAProfileRepository
                    .findTopByDonor_DonorIdOrderByEventTimestampDesc(donation.getDonorId())
                    .orElse(null);

            if (hlaProfile != null) {
                hlaA1 = hlaProfile.getHlaA1();
                hlaA2 = hlaProfile.getHlaA2();
                hlaB1 = hlaProfile.getHlaB1();
                hlaB2 = hlaProfile.getHlaB2();
                hlaC1 = hlaProfile.getHlaC1();
                hlaC2 = hlaProfile.getHlaC2();
                hlaDR1 = hlaProfile.getHlaDR1();
                hlaDR2 = hlaProfile.getHlaDR2();
                hlaDQ1 = hlaProfile.getHlaDQ1();
                hlaDQ2 = hlaProfile.getHlaDQ2();
                hlaDP1 = hlaProfile.getHlaDP1();
                hlaDP2 = hlaProfile.getHlaDP2();
                hlaString = hlaProfile.getHlaString();
                hlaHighRes = hlaProfile.getIsHighResolution();
            }
        }

        MLDonationData.MLDonationDataBuilder builder = MLDonationData.builder()
                .donationId(donation.getDonationId())
                .donorId(donation.getDonorId())
                .userId(donation.getUserId())
                .locationId(location != null ? location.getLocationId() : null)
                .donationType(donation.getDonationType())
                .bloodType(donation.getBloodType())
                .donationDate(donation.getDonationDate())
                .age(eligibility != null ? eligibility.getAge() : null)
                .dob(eligibility != null ? eligibility.getDob() : null)
                .weight(eligibility != null ? eligibility.getWeight() : null)
                .height(eligibility != null ? eligibility.getHeight() : null)
                .bmi(eligibility != null ? eligibility.getBodyMassIndex() : null)
                .bodySize(eligibility != null ? eligibility.getBodySize() : null)
                .isLivingDonor(eligibility != null ? eligibility.getIsLivingDonor() : null)
                .hemoglobinLevel(medical != null ? medical.getHemoglobinLevel() : null)
                .bloodGlucoseLevel(medical != null ? medical.getBloodGlucoseLevel() : null)
                .hasDiabetes(medical != null ? medical.getHasDiabetes() : false)
                .bloodPressure(medical != null ? medical.getBloodPressure() : null)
                .hasDiseases(medical != null ? medical.getHasDiseases() : false)
                .hasInfectiousDiseases(medical != null ? medical.getHasInfectiousDiseases() : false)
                .infectiousDiseaseDetails(medical != null ? medical.getInfectiousDiseaseDetails() : null)
                .creatinineLevel(medical != null ? medical.getCreatinineLevel() : null)
                .liverFunctionTests(medical != null ? medical.getLiverFunctionTests() : null)
                .cardiacStatus(medical != null ? medical.getCardiacStatus() : null)
                .pulmonaryFunction(medical != null ? medical.getPulmonaryFunction() : null)
                .overallHealthStatus(medical != null ? medical.getOverallHealthStatus() : null)
                .medicalClearance(eligibility != null ? eligibility.getMedicalClearance() : null)
                .recentTattoo(eligibility != null ? eligibility.getRecentTattooOrPiercing() : null)
                .recentVaccination(eligibility != null ? eligibility.getRecentVaccination() : null)
                .recentSurgery(eligibility != null ? eligibility.getRecentSurgery() : null)
                .chronicDiseases(eligibility != null ? eligibility.getChronicDiseases() : null)
                .allergies(eligibility != null ? eligibility.getAllergies() : null)
                .lastDonationDate(eligibility != null ? eligibility.getLastDonationDate() : null)
                .daysSinceLastDonation(daysSinceLastDonation)
                .smokingStatus(eligibility != null ? eligibility.getSmokingStatus() : null)
                .packYears(eligibility != null ? eligibility.getPackYears() : null)
                .quitSmokingDate(eligibility != null ? eligibility.getQuitSmokingDate() : null)
                .alcoholStatus(eligibility != null ? eligibility.getAlcoholStatus() : null)
                .drinksPerWeek(eligibility != null ? eligibility.getDrinksPerWeek() : null)
                .quitAlcoholDate(eligibility != null ? eligibility.getQuitAlcoholDate() : null)
                .alcoholAbstinenceMonths(eligibility != null ? eligibility.getAlcoholAbstinenceMonths() : null)
                .latitude(location != null ? location.getLatitude() : null)
                .longitude(location != null ? location.getLongitude() : null)
                .city(location != null ? location.getCity() : null)
                .district(location != null ? location.getDistrict() : null)
                .state(location != null ? location.getState() : null)
                .country(location != null ? location.getCountry() : null)
                .hlaA1(hlaA1)
                .hlaA2(hlaA2)
                .hlaB1(hlaB1)
                .hlaB2(hlaB2)
                .hlaC1(hlaC1)
                .hlaC2(hlaC2)
                .hlaDR1(hlaDR1)
                .hlaDR2(hlaDR2)
                .hlaDQ1(hlaDQ1)
                .hlaDQ2(hlaDQ2)
                .hlaDP1(hlaDP1)
                .hlaDP2(hlaDP2)
                .hlaHighResolution(hlaHighRes)
                .hlaString(hlaString);

        if (donation instanceof BloodDonation) {
            BloodDonation blood = (BloodDonation) donation;
            builder.quantity(blood.getQuantity());

        } else if (donation instanceof OrganDonation) {
            OrganDonation organ = (OrganDonation) donation;
            builder
                    .organType(organ.getOrganType())
                    .organQuality(organ.getOrganQuality())
                    .organViabilityExpiry(organ.getOrganViabilityExpiry())
                    .coldIschemiaTime(organ.getColdIschemiaTime())
                    .organPerfused(organ.getOrganPerfused())
                    .organWeight(organ.getOrganWeight())
                    .organSize(organ.getOrganSize())
                    .hasAbnormalities(organ.getHasAbnormalities());

            if (organ.getOrganViabilityExpiry() != null) {
                long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), organ.getOrganViabilityExpiry());
                builder.organViabilityHours((double) Math.max(0, hours));
            }

        } else if (donation instanceof TissueDonation) {
            TissueDonation tissue = (TissueDonation) donation;
            builder
                    .tissueType(tissue.getTissueType())
                    .quantity(tissue.getQuantity());

        } else if (donation instanceof StemCellDonation) {
            StemCellDonation stemCell = (StemCellDonation) donation;
            builder
                    .stemCellType(stemCell.getStemCellType())
                    .quantity(stemCell.getQuantity());
        }

        return builder.build();
    }
}
