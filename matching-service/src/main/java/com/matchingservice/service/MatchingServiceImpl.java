package com.matchingservice.service;

import com.matchingservice.dto.*;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.MatchStatus;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.model.MatchResult;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.model.recipients.RecipientHLAProfile;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.repository.*;
import com.matchingservice.client.DonorServiceClient;
import com.matchingservice.client.RecipientServiceClient;
import com.matchingservice.repository.donor.*;
import com.matchingservice.repository.recipient.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchResultRepository matchResultRepository;
    private final DonationRepository donationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final RecipientLocationRepository recipientLocationRepository;
    private final RecipientRepository recipientRepository;
    private final DonorRepository donorRepository;
    private final DonorHLAProfileRepository donorHLAProfileRepository;
    private final RecipientHLAProfileRepository recipientHLAProfileRepository;
    private final DonorServiceClient donorServiceClient;
    private final RecipientServiceClient recipientServiceClient;

    @Override
    @Transactional
    public ManualMatchResponse manualMatch(ManualMatchRequest request) {
        System.out.println("Attempting manual match: Donation " + request.getDonationId() +
                " with ReceiveRequest " + request.getReceiveRequestId());

        try {
            Donation donation = donationRepository.findById(request.getDonationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Donation not found with ID: " + request.getDonationId()));
            ReceiveRequest receiveRequest = receiveRequestRepository.findById(request.getReceiveRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ReceiveRequest not found with ID: " + request.getReceiveRequestId()));

            DonorLocation donorLocation = null;
            if (request.getDonorLocationId() != null) {
                donorLocation = donorLocationRepository
                        .findTopByLocationIdOrderByEventTimestampDesc(request.getDonorLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Donor location not found with ID: " + request.getDonorLocationId()));
            }

            RecipientLocation recipientLocation = null;
            if (request.getRecipientLocationId() != null) {
                recipientLocation = recipientLocationRepository
                        .findTopByLocationIdOrderByEventTimestampDesc(request.getRecipientLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Recipient location not found with ID: " + request.getRecipientLocationId()));
            }

            Recipient recipient = recipientRepository
                    .findTopByRecipientIdOrderByEventTimestampDesc(receiveRequest.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

            MatchResult matchResult = new MatchResult();
            matchResult.setDonationId(donation.getDonationId());
            matchResult.setReceiveRequestId(receiveRequest.getReceiveRequestId());
            matchResult.setDonorUserId(donation.getDonor().getUserId());
            matchResult.setRecipientUserId(recipient.getUserId());
            matchResult.setDonorLocationId(donorLocation != null ? donorLocation.getLocationId() : null);
            matchResult.setRecipientLocationId(recipientLocation != null ? recipientLocation.getLocationId() : null);
            matchResult.setMatchedAt(LocalDateTime.now());
            matchResult.setStatus(MatchStatus.PENDING);
            matchResult.setIsConfirmed(false);
            matchResult.setDistance(calculateDistance(donorLocation, recipientLocation));

            MatchResult savedMatchResult = matchResultRepository.save(matchResult);

            System.out.println("Successfully created manual match result: " + savedMatchResult.getId());
            ManualMatchResponse.MatchDetails matchDetails = ManualMatchResponse.MatchDetails.builder()
                    .donationId(donation.getDonationId())
                    .receiveRequestId(receiveRequest.getReceiveRequestId())
                    .donorUserId(donation.getDonor().getUserId())
                    .recipientUserId(recipient.getUserId())
                    .donationType(donation.getDonationType() != null ? donation.getDonationType().toString() : null)
                    .requestType(receiveRequest.getRequestType() != null ? receiveRequest.getRequestType().toString() : null)
                    .bloodType(donation.getBloodType() != null ? donation.getBloodType().toString() : null)
                    .matchType("DONOR_TO_RECIPIENT")
                    .build();

            return ManualMatchResponse.builder()
                    .success(true)
                    .message("Manual match successful. MatchResult ID: " + savedMatchResult.getId())
                    .matchResultId(savedMatchResult.getId())
                    .matchDetails(matchDetails)
                    .build();
        } catch (ResourceNotFoundException e) {
            System.err.println("Validation Error during manual match: " + e.getMessage());
            return ManualMatchResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .matchResultId(null)
                    .matchDetails(null)
                    .build();
        } catch (Exception e) {
            System.err.println("Error during manual match: " + e.getMessage());
            e.printStackTrace();
            return ManualMatchResponse.builder()
                    .success(false)
                    .message("Failed to perform manual match: " + e.getMessage())
                    .matchResultId(null)
                    .matchDetails(null)
                    .build();
        }
    }

    @Override
    public List<MatchResponse> getAllMatches() {
        return matchResultRepository.findAll()
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    private MatchResponse enrichMatchResponse(MatchResult matchResult, String matchType) {
        MatchResponse response = MatchResponse.fromMatchResult(matchResult);
        response.setMatchType(matchType);

        donationRepository.findById(matchResult.getDonationId())
                .ifPresent(donation -> {
                    response.setDonationType(donation.getDonationType() != null ? donation.getDonationType().toString() : null);
                    response.setBloodType(donation.getBloodType() != null ? donation.getBloodType().toString() : null);
                });

        receiveRequestRepository.findById(matchResult.getReceiveRequestId())
                .ifPresent(request -> {
                    response.setRequestType(request.getRequestType() != null ? request.getRequestType().toString() : null);
                });

        return response;
    }

    @Override
    public List<MatchResponse> getMatchesByDonation(UUID donationId) {
        return matchResultRepository.findByDonationIdOrderByCompatibilityScoreDesc(donationId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesByRequest(UUID receiveRequestId) {
        return matchResultRepository.findByReceiveRequestIdOrderByCompatibilityScoreDesc(receiveRequestId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "RECIPIENT_TO_DONOR"))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String donorConfirmMatch(UUID matchId, UUID userId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));

        if (!matchResult.getDonorUserId().equals(userId)) {
            throw new IllegalStateException("Only the donor can confirm this match");
        }

        if (matchResult.getDonorConfirmed()) {
            return "Donor has already confirmed this match";
        }

        matchResult.setDonorConfirmed(true);
        matchResult.setDonorConfirmedAt(LocalDateTime.now());

        if (matchResult.getRecipientConfirmed()) {
            finalizeMatch(matchResult);
            matchResultRepository.save(matchResult);
            return "Match fully confirmed! Both donor and recipient have agreed. Donation process initiated.";
        }

        matchResultRepository.save(matchResult);
        return "Donor confirmation recorded. Waiting for recipient confirmation.";
    }

    @Override
    @Transactional
    public String recipientConfirmMatch(UUID matchId, UUID userId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));

        if (!matchResult.getRecipientUserId().equals(userId)) {
            throw new IllegalStateException("Only the recipient can confirm this match");
        }

        if (matchResult.getRecipientConfirmed()) {
            return "Recipient has already confirmed this match";
        }

        matchResult.setRecipientConfirmed(true);
        matchResult.setRecipientConfirmedAt(LocalDateTime.now());

        if (matchResult.getDonorConfirmed()) {
            finalizeMatch(matchResult);
            matchResultRepository.save(matchResult);
            return "Match fully confirmed! Both donor and recipient have agreed. Donation process initiated.";
        }

        matchResultRepository.save(matchResult);
        return "Recipient confirmation recorded. Waiting for donor confirmation.";
    }

    private void finalizeMatch(MatchResult matchResult) {
        matchResult.setIsConfirmed(true);
        matchResult.setStatus(MatchStatus.CONFIRMED);

        expireConflictingMatches(matchResult);

        donationRepository.findById(matchResult.getDonationId())
                .ifPresent(donation -> {
                    donation.setStatus(DonationStatus.COMPLETED);
                    donationRepository.save(donation);
                });

        receiveRequestRepository.findById(matchResult.getReceiveRequestId())
                .ifPresent(request -> {
                    request.setStatus(RequestStatus.FULFILLED);
                    receiveRequestRepository.save(request);
                });

        try {
            donorServiceClient.updateDonationStatusToCompleted(matchResult.getDonationId());
        } catch (Exception e) {
            System.err.println("Failed to update donation status in donor-service: " + e.getMessage());
        }

        try {
            recipientServiceClient.updateRequestStatusToFulfilled(matchResult.getReceiveRequestId());
        } catch (Exception e) {
            System.err.println("Failed to update request status in recipient-service: " + e.getMessage());
        }
    }

    @Transactional
    public void expireConflictingMatches(MatchResult confirmedMatch) {
        matchResultRepository.expirePendingDonationMatches(
                confirmedMatch.getDonationId(),
                confirmedMatch.getId()
        );

        matchResultRepository.expirePendingRequestMatches(
                confirmedMatch.getReceiveRequestId(),
                confirmedMatch.getId()
        );

        System.out.println("Expired conflicting matches for donation: " + confirmedMatch.getDonationId() +
                " and request: " + confirmedMatch.getReceiveRequestId());
    }

    @Override
    public List<MatchResponse> getMatchesForDonor(UUID donorUserId) {
        return matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(donorUserId)
                .stream()
                .filter(match -> match.getStatus() != MatchStatus.EXPIRED)
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesForRecipient(UUID recipientUserId) {
        return matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(recipientUserId)
                .stream()
                .filter(match -> match.getStatus() != MatchStatus.EXPIRED)
                .map(matchResult -> enrichMatchResponse(matchResult, "RECIPIENT_TO_DONOR"))
                .collect(Collectors.toList());
    }


    @Override
    public boolean hasAccessToDonation(UUID donationId, UUID userId) {
        return donationRepository.findById(donationId)
                .map(donation -> donation.getDonor().getUserId().equals(userId))
                .orElse(false)
                || matchResultRepository.existsByDonationIdAndRecipientUserId(donationId, userId);
    }


    @Override
    public boolean hasAccessToRequest(UUID requestId, UUID userId) {
        return receiveRequestRepository.findById(requestId)
                .map(request -> request.getRecipient().getUserId().equals(userId))
                .orElse(false)
                || matchResultRepository.existsByReceiveRequestIdAndDonorUserId(requestId, userId);
    }

    @Override
    public boolean hasAccessToDonorSnapshot(UUID donationId, UUID userId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        if (donation.getDonor().getUserId().equals(userId)) {
            return true;
        }

        return matchResultRepository.existsByDonationIdAndRecipientUserId(donationId, userId);
    }

    @Override
    public boolean hasAccessToRecipientSnapshot(UUID requestId, UUID userId) {
        ReceiveRequest request = receiveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getRecipient().getUserId().equals(userId)) {
            return true;
        }

        return matchResultRepository.existsByReceiveRequestIdAndDonorUserId(requestId, userId);
    }



    @Override
    public DonationDTO getDonationById(UUID donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        return convertToDTO(donation);
    }

    @Override
    public ReceiveRequestDTO getRequestById(UUID requestId) {
        ReceiveRequest request = receiveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        return convertToDTO(request);
    }

    @Override
    public DonorDTO getDonorByUserId(UUID userId) {
        Donor donor = donorRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found for user: " + userId));
        return convertDonorToDTO(donor);
    }

    @Override
    public RecipientDTO getRecipientByUserId(UUID userId) {
        Recipient recipient = recipientRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found for user: " + userId));
        return convertRecipientToDTO(recipient);
    }

    public DonorDTO getDonorSnapshotByDonation(UUID donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + donationId));

        Donor historicalDonor = donation.getDonor();
        return convertDonorToDTO(historicalDonor);
    }

    public RecipientDTO getRecipientSnapshotByRequest(UUID requestId) {
        ReceiveRequest request = receiveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        Recipient historicalRecipient = request.getRecipient();
        return convertRecipientToDTO(historicalRecipient);
    }

    @Override
    public List<MatchResponse> getActiveMatchesForUser(UUID userId) {
        List<MatchResult> donorMatches = matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(userId)
                .stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING || match.getStatus() == MatchStatus.CONFIRMED)
                .collect(Collectors.toList());

        List<MatchResult> recipientMatches = matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(userId)
                .stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING || match.getStatus() == MatchStatus.CONFIRMED)
                .collect(Collectors.toList());

        List<MatchResult> allActiveMatches = new ArrayList<>();
        allActiveMatches.addAll(donorMatches);
        allActiveMatches.addAll(recipientMatches);

        return allActiveMatches.stream()
                .distinct()
                .sorted((m1, m2) -> m2.getMatchedAt().compareTo(m1.getMatchedAt()))
                .map(match -> {
                    boolean isDonor = match.getDonorUserId().equals(userId);
                    return enrichMatchResponse(match, isDonor ? "DONOR_TO_RECIPIENT" : "RECIPIENT_TO_DONOR");
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getPendingMatchesForUser(UUID userId) {
        return getActiveMatchesForUser(userId)
                .stream()
                .filter(match -> MatchStatus.PENDING.equals(match.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getConfirmedMatchesForUser(UUID userId) {
        return getActiveMatchesForUser(userId)
                .stream()
                .filter(match -> MatchStatus.CONFIRMED.equals(match.getStatus()))
                .collect(Collectors.toList());
    }

    private DonorDTO convertDonorToDTO(Donor donor) {
        DonorDTO dto = new DonorDTO();
        dto.setDonorId(donor.getDonorId());
        dto.setUserId(donor.getUserId());
        dto.setRegistrationDate(donor.getRegistrationDate());
        dto.setStatus(donor.getStatus());

        if (donor.getMedicalDetails() != null) {
            DonorDTO.DonorMedicalDetailsDTO medicalDetails = new DonorDTO.DonorMedicalDetailsDTO();
            medicalDetails.setMedicalDetailsId(donor.getMedicalDetails().getMedicalDetailsId());
            medicalDetails.setBloodGlucoseLevel(donor.getMedicalDetails().getBloodGlucoseLevel());
            medicalDetails.setHasDiabetes(donor.getMedicalDetails().getHasDiabetes());
            medicalDetails.setHemoglobinLevel(donor.getMedicalDetails().getHemoglobinLevel());
            medicalDetails.setBloodPressure(donor.getMedicalDetails().getBloodPressure());
            medicalDetails.setHasDiseases(donor.getMedicalDetails().getHasDiseases());
            medicalDetails.setTakingMedication(donor.getMedicalDetails().getTakingMedication());
            medicalDetails.setDiseaseDescription(donor.getMedicalDetails().getDiseaseDescription());
            medicalDetails.setCurrentMedications(donor.getMedicalDetails().getCurrentMedications());
            medicalDetails.setLastMedicalCheckup(donor.getMedicalDetails().getLastMedicalCheckup());
            medicalDetails.setMedicalHistory(donor.getMedicalDetails().getMedicalHistory());
            medicalDetails.setHasInfectiousDiseases(donor.getMedicalDetails().getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(donor.getMedicalDetails().getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(donor.getMedicalDetails().getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(donor.getMedicalDetails().getLiverFunctionTests());
            medicalDetails.setCardiacStatus(donor.getMedicalDetails().getCardiacStatus());
            medicalDetails.setPulmonaryFunction(donor.getMedicalDetails().getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(donor.getMedicalDetails().getOverallHealthStatus());
            dto.setMedicalDetails(medicalDetails);
        }

        if (donor.getEligibilityCriteria() != null) {
            DonorDTO.DonorEligibilityCriteriaDTO eligibility = new DonorDTO.DonorEligibilityCriteriaDTO();
            eligibility.setEligibilityCriteriaId(donor.getEligibilityCriteria().getEligibilityCriteriaId());
            eligibility.setWeight(donor.getEligibilityCriteria().getWeight());
            eligibility.setAge(donor.getEligibilityCriteria().getAge());
            eligibility.setDob(donor.getEligibilityCriteria().getDob());
            eligibility.setMedicalClearance(donor.getEligibilityCriteria().getMedicalClearance());
            eligibility.setRecentTattooOrPiercing(donor.getEligibilityCriteria().getRecentTattooOrPiercing());
            eligibility.setRecentTravelDetails(donor.getEligibilityCriteria().getRecentTravelDetails());
            eligibility.setRecentVaccination(donor.getEligibilityCriteria().getRecentVaccination());
            eligibility.setRecentSurgery(donor.getEligibilityCriteria().getRecentSurgery());
            eligibility.setChronicDiseases(donor.getEligibilityCriteria().getChronicDiseases());
            eligibility.setAllergies(donor.getEligibilityCriteria().getAllergies());
            eligibility.setLastDonationDate(donor.getEligibilityCriteria().getLastDonationDate());
            eligibility.setHeight(donor.getEligibilityCriteria().getHeight());
            eligibility.setBodyMassIndex(donor.getEligibilityCriteria().getBodyMassIndex());
            eligibility.setBodySize(donor.getEligibilityCriteria().getBodySize());
            eligibility.setIsLivingDonor(donor.getEligibilityCriteria().getIsLivingDonor());
            eligibility.setSmokingStatus(donor.getEligibilityCriteria().getSmokingStatus());
            eligibility.setPackYears(donor.getEligibilityCriteria().getPackYears());
            eligibility.setQuitSmokingDate(donor.getEligibilityCriteria().getQuitSmokingDate());
            eligibility.setAlcoholStatus(donor.getEligibilityCriteria().getAlcoholStatus());
            eligibility.setDrinksPerWeek(donor.getEligibilityCriteria().getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(donor.getEligibilityCriteria().getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(donor.getEligibilityCriteria().getAlcoholAbstinenceMonths());
            dto.setEligibilityCriteria(eligibility);
        }

        donorHLAProfileRepository.findTopByDonor_DonorIdOrderByEventTimestampDesc(donor.getDonorId())
                .ifPresent(hlaProfile -> dto.setHlaProfile(convertHLAProfileToDTO(hlaProfile)));

        List<LocationDTO> locations = donorLocationRepository.findLatestLocationsByDonorId(donor.getDonorId())
                .stream()
                .map(this::convertLocationToDTO)
                .collect(Collectors.toList());
        dto.setLocations(locations);

        return dto;
    }

    private RecipientDTO convertRecipientToDTO(Recipient recipient) {
        RecipientDTO dto = new RecipientDTO();
        dto.setRecipientId(recipient.getRecipientId());
        dto.setUserId(recipient.getUserId());
        dto.setAvailability(recipient.getAvailability());

        if (recipient.getMedicalDetails() != null) {
            RecipientDTO.RecipientMedicalDetailsDTO medicalDetails = new RecipientDTO.RecipientMedicalDetailsDTO();
            medicalDetails.setMedicalDetailsId(recipient.getMedicalDetails().getMedicalDetailsId());
            medicalDetails.setHemoglobinLevel(recipient.getMedicalDetails().getHemoglobinLevel());
            medicalDetails.setBloodGlucoseLevel(recipient.getMedicalDetails().getBloodGlucoseLevel());
            medicalDetails.setHasDiabetes(recipient.getMedicalDetails().getHasDiabetes());
            medicalDetails.setBloodPressure(recipient.getMedicalDetails().getBloodPressure());
            medicalDetails.setDiagnosis(recipient.getMedicalDetails().getDiagnosis());
            medicalDetails.setAllergies(recipient.getMedicalDetails().getAllergies());
            medicalDetails.setCurrentMedications(recipient.getMedicalDetails().getCurrentMedications());
            medicalDetails.setAdditionalNotes(recipient.getMedicalDetails().getAdditionalNotes());
            medicalDetails.setHasInfectiousDiseases(recipient.getMedicalDetails().getHasInfectiousDiseases());
            medicalDetails.setInfectiousDiseaseDetails(recipient.getMedicalDetails().getInfectiousDiseaseDetails());
            medicalDetails.setCreatinineLevel(recipient.getMedicalDetails().getCreatinineLevel());
            medicalDetails.setLiverFunctionTests(recipient.getMedicalDetails().getLiverFunctionTests());
            medicalDetails.setCardiacStatus(recipient.getMedicalDetails().getCardiacStatus());
            medicalDetails.setPulmonaryFunction(recipient.getMedicalDetails().getPulmonaryFunction());
            medicalDetails.setOverallHealthStatus(recipient.getMedicalDetails().getOverallHealthStatus());
            dto.setMedicalDetails(medicalDetails);
        }

        if (recipient.getEligibilityCriteria() != null) {
            RecipientDTO.RecipientEligibilityCriteriaDTO eligibility = new RecipientDTO.RecipientEligibilityCriteriaDTO();
            eligibility.setEligibilityCriteriaId(recipient.getEligibilityCriteria().getEligibilityCriteriaId());
            eligibility.setAgeEligible(recipient.getEligibilityCriteria().getAgeEligible());
            eligibility.setAge(recipient.getEligibilityCriteria().getAge());
            eligibility.setDob(recipient.getEligibilityCriteria().getDob());
            eligibility.setWeightEligible(recipient.getEligibilityCriteria().getWeightEligible());
            eligibility.setWeight(recipient.getEligibilityCriteria().getWeight());
            eligibility.setMedicallyEligible(recipient.getEligibilityCriteria().getMedicallyEligible());
            eligibility.setLegalClearance(recipient.getEligibilityCriteria().getLegalClearance());
            eligibility.setNotes(recipient.getEligibilityCriteria().getNotes());
            eligibility.setLastReviewed(recipient.getEligibilityCriteria().getLastReviewed());
            eligibility.setHeight(recipient.getEligibilityCriteria().getHeight());
            eligibility.setBodyMassIndex(recipient.getEligibilityCriteria().getBodyMassIndex());
            eligibility.setBodySize(recipient.getEligibilityCriteria().getBodySize());
            eligibility.setIsLivingDonor(recipient.getEligibilityCriteria().getIsLivingDonor());
            eligibility.setSmokingStatus(recipient.getEligibilityCriteria().getSmokingStatus());
            eligibility.setPackYears(recipient.getEligibilityCriteria().getPackYears());
            eligibility.setQuitSmokingDate(recipient.getEligibilityCriteria().getQuitSmokingDate());
            eligibility.setAlcoholStatus(recipient.getEligibilityCriteria().getAlcoholStatus());
            eligibility.setDrinksPerWeek(recipient.getEligibilityCriteria().getDrinksPerWeek());
            eligibility.setQuitAlcoholDate(recipient.getEligibilityCriteria().getQuitAlcoholDate());
            eligibility.setAlcoholAbstinenceMonths(recipient.getEligibilityCriteria().getAlcoholAbstinenceMonths());
            dto.setEligibilityCriteria(eligibility);
        }

        recipientHLAProfileRepository.findTopByRecipient_RecipientIdOrderByEventTimestampDesc(recipient.getRecipientId())
                .ifPresent(hlaProfile -> dto.setHlaProfile(convertHLAProfileToDTO(hlaProfile)));

        List<LocationDTO> locations = recipientLocationRepository.findLatestLocationsByRecipientId(recipient.getRecipientId())
                .stream()
                .map(this::convertLocationToDTO)
                .collect(Collectors.toList());
        dto.setLocations(locations);

        return dto;
    }

    private DonationDTO convertToDTO(Donation donation) {
        DonationDTO dto = new DonationDTO();
        dto.setId(donation.getDonationId());
        dto.setDonorId(donation.getDonorId());
        dto.setDonationType(donation.getDonationType());
        dto.setDonationDate(donation.getDonationDate());
        dto.setStatus(donation.getStatus());
        dto.setBloodType(donation.getBloodType());

        if (donation instanceof BloodDonation) {
            dto.setQuantity(((BloodDonation) donation).getQuantity());
        } else if (donation instanceof OrganDonation) {
            OrganDonation organ = (OrganDonation) donation;
            dto.setOrganType(organ.getOrganType());
            dto.setIsCompatible(organ.getIsCompatible());
            dto.setOrganQuality(organ.getOrganQuality());
            dto.setOrganViabilityExpiry(organ.getOrganViabilityExpiry());
            dto.setColdIschemiaTime(organ.getColdIschemiaTime());
            dto.setOrganPerfused(organ.getOrganPerfused());
            dto.setOrganWeight(organ.getOrganWeight());
            dto.setOrganSize(organ.getOrganSize());
            dto.setFunctionalAssessment(organ.getFunctionalAssessment());
            dto.setHasAbnormalities(organ.getHasAbnormalities());
            dto.setAbnormalityDescription(organ.getAbnormalityDescription());
        } else if (donation instanceof TissueDonation) {
            TissueDonation tissue = (TissueDonation) donation;
            dto.setTissueType(tissue.getTissueType());
            dto.setQuantity(tissue.getQuantity());
        } else if (donation instanceof StemCellDonation) {
            StemCellDonation stemCell = (StemCellDonation) donation;
            dto.setStemCellType(stemCell.getStemCellType());
            dto.setQuantity(stemCell.getQuantity());
        }

        if (donation.getLocation() != null) {
            dto.setLocation(convertLocationToDTO(donation.getLocation()));
        }

        return dto;
    }

    private ReceiveRequestDTO convertToDTO(ReceiveRequest receiveRequest) {
        ReceiveRequestDTO dto = new ReceiveRequestDTO();
        dto.setId(receiveRequest.getReceiveRequestId());
        dto.setRecipientId(receiveRequest.getRecipientId());
        dto.setRequestType(receiveRequest.getRequestType());
        dto.setRequestedBloodType(receiveRequest.getRequestedBloodType());
        dto.setRequestedOrgan(receiveRequest.getRequestedOrgan());
        dto.setRequestedTissue(receiveRequest.getRequestedTissue());
        dto.setRequestedStemCellType(receiveRequest.getRequestedStemCellType());
        dto.setUrgencyLevel(receiveRequest.getUrgencyLevel());
        dto.setQuantity(receiveRequest.getQuantity());
        dto.setRequestDate(receiveRequest.getRequestDate());
        dto.setStatus(receiveRequest.getStatus());
        dto.setNotes(receiveRequest.getNotes());

        if (receiveRequest.getLocation() != null) {
            dto.setLocation(convertLocationToDTO(receiveRequest.getLocation()));
        }

        return dto;
    }

    private LocationDTO convertLocationToDTO(DonorLocation location) {
        LocationDTO dto = new LocationDTO();
        dto.setLocationId(location.getLocationId());
        dto.setAddressLine(location.getAddressLine());
        dto.setLandmark(location.getLandmark());
        dto.setArea(location.getArea());
        dto.setCity(location.getCity());
        dto.setDistrict(location.getDistrict());
        dto.setState(location.getState());
        dto.setCountry(location.getCountry());
        dto.setPincode(location.getPincode());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        return dto;
    }

    private LocationDTO convertLocationToDTO(RecipientLocation location) {
        LocationDTO dto = new LocationDTO();
        dto.setLocationId(location.getLocationId());
        dto.setAddressLine(location.getAddressLine());
        dto.setLandmark(location.getLandmark());
        dto.setArea(location.getArea());
        dto.setCity(location.getCity());
        dto.setDistrict(location.getDistrict());
        dto.setState(location.getState());
        dto.setCountry(location.getCountry());
        dto.setPincode(location.getPincode());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        return dto;
    }

    private HLAProfileDTO convertHLAProfileToDTO(DonorHLAProfile hlaProfile) {
        HLAProfileDTO dto = new HLAProfileDTO();
        dto.setId(hlaProfile.getId());
        dto.setHlaA1(hlaProfile.getHlaA1());
        dto.setHlaA2(hlaProfile.getHlaA2());
        dto.setHlaB1(hlaProfile.getHlaB1());
        dto.setHlaB2(hlaProfile.getHlaB2());
        dto.setHlaC1(hlaProfile.getHlaC1());
        dto.setHlaC2(hlaProfile.getHlaC2());
        dto.setHlaDR1(hlaProfile.getHlaDR1());
        dto.setHlaDR2(hlaProfile.getHlaDR2());
        dto.setHlaDQ1(hlaProfile.getHlaDQ1());
        dto.setHlaDQ2(hlaProfile.getHlaDQ2());
        dto.setHlaDP1(hlaProfile.getHlaDP1());
        dto.setHlaDP2(hlaProfile.getHlaDP2());
        dto.setTestingDate(hlaProfile.getTestingDate());
        dto.setTestingMethod(hlaProfile.getTestingMethod());
        dto.setLaboratoryName(hlaProfile.getLaboratoryName());
        dto.setCertificationNumber(hlaProfile.getCertificationNumber());
        dto.setHlaString(hlaProfile.getHlaString());
        dto.setIsHighResolution(hlaProfile.getIsHighResolution());
        return dto;
    }

    private HLAProfileDTO convertHLAProfileToDTO(RecipientHLAProfile hlaProfile) {
        HLAProfileDTO dto = new HLAProfileDTO();
        dto.setId(hlaProfile.getId());
        dto.setHlaA1(hlaProfile.getHlaA1());
        dto.setHlaA2(hlaProfile.getHlaA2());
        dto.setHlaB1(hlaProfile.getHlaB1());
        dto.setHlaB2(hlaProfile.getHlaB2());
        dto.setHlaC1(hlaProfile.getHlaC1());
        dto.setHlaC2(hlaProfile.getHlaC2());
        dto.setHlaDR1(hlaProfile.getHlaDR1());
        dto.setHlaDR2(hlaProfile.getHlaDR2());
        dto.setHlaDQ1(hlaProfile.getHlaDQ1());
        dto.setHlaDQ2(hlaProfile.getHlaDQ2());
        dto.setHlaDP1(hlaProfile.getHlaDP1());
        dto.setHlaDP2(hlaProfile.getHlaDP2());
        dto.setTestingDate(hlaProfile.getTestingDate());
        dto.setTestingMethod(hlaProfile.getTestingMethod());
        dto.setLaboratoryName(hlaProfile.getLaboratoryName());
        dto.setCertificationNumber(hlaProfile.getCertificationNumber());
        dto.setHlaString(hlaProfile.getHlaString());
        dto.setIsHighResolution(hlaProfile.getIsHighResolution());
        return dto;
    }

    private Double calculateDistance(DonorLocation donorLoc, RecipientLocation recipientLoc) {
        if (donorLoc == null || recipientLoc == null ||
                donorLoc.getLatitude() == null || donorLoc.getLongitude() == null ||
                recipientLoc.getLatitude() == null || recipientLoc.getLongitude() == null) {
            return null;
        }

        final int R = 6371;

        double latDistance = Math.toRadians(recipientLoc.getLatitude() - donorLoc.getLatitude());
        double lonDistance = Math.toRadians(recipientLoc.getLongitude() - donorLoc.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(donorLoc.getLatitude())) * Math.cos(Math.toRadians(recipientLoc.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Override
    public boolean isMatchConfirmed(UUID matchId) {
        return matchResultRepository.findById(matchId)
                .map(matchResult -> Boolean.TRUE.equals(matchResult.getIsConfirmed()))
                .orElse(false);
    }
}
