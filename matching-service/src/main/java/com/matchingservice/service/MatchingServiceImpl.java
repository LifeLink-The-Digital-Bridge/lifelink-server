package com.matchingservice.service;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;
import com.matchingservice.dto.MatchResponse;
import com.matchingservice.dto.CreateDonationHistoryRequest;
import com.matchingservice.dto.CreateRecipientHistoryRequest;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.model.donor.Donation;
import com.matchingservice.model.donor.DonorLocation;
import com.matchingservice.model.donor.MatchResult;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.repository.*;
import com.matchingservice.model.donor.Donor;
import com.matchingservice.model.donor.DonorHLAProfile;
import com.matchingservice.model.recipients.RecipientHLAProfile;
import com.matchingservice.client.DonorServiceClient;
import com.matchingservice.client.RecipientServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                donorLocation = donorLocationRepository.findById(request.getDonorLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Donor location not found with ID: " + request.getDonorLocationId()));
            }
            RecipientLocation recipientLocation = null;
            if (request.getRecipientLocationId() != null) {
                recipientLocation = recipientLocationRepository.findById(request.getRecipientLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Recipient location not found with ID: " + request.getRecipientLocationId()));
            }

            Recipient recipient = recipientRepository.findById(receiveRequest.getRecipientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

            MatchResult matchResult = new MatchResult();
            matchResult.setDonationId(donation.getDonationId());
            matchResult.setReceiveRequestId(receiveRequest.getReceiveRequestId());
            matchResult.setDonorUserId(donation.getUserId());
            matchResult.setRecipientUserId(recipient.getUserId());
            matchResult.setDonorLocationId(donorLocation != null ? donorLocation.getLocationId() : null);
            matchResult.setRecipientLocationId(recipientLocation != null ? recipientLocation.getLocationId() : null);
            matchResult.setMatchedAt(LocalDateTime.now());
            matchResult.setIsConfirmed(false);
            matchResult.setDistance(0.0);

            MatchResult savedMatchResult = matchResultRepository.save(matchResult);

            System.out.println("Successfully created manual match result: " + savedMatchResult.getId());
            ManualMatchResponse.MatchDetails matchDetails = ManualMatchResponse.MatchDetails.builder()
                    .donationId(donation.getDonationId())
                    .receiveRequestId(receiveRequest.getReceiveRequestId())
                    .donorUserId(donation.getUserId())
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
    public List<MatchResponse> getMatchesForDonor(UUID donorUserId) {
        return matchResultRepository.findByDonorUserId(donorUserId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesForRecipient(UUID recipientUserId) {
        return matchResultRepository.findByRecipientUserId(recipientUserId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "RECIPIENT_TO_DONOR"))
                .collect(Collectors.toList());
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
        return matchResultRepository.findByDonationId(donationId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesByRequest(UUID receiveRequestId) {
        return matchResultRepository.findByReceiveRequestId(receiveRequestId)
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
        LocalDateTime completedAt = LocalDateTime.now();
        
        try {
            CreateDonationHistoryRequest donationHistoryRequest = buildDonationHistoryRequest(
                    matchResult, completedAt);
            donorServiceClient.createDonationHistory(donationHistoryRequest);
            System.out.println("Donation history created successfully");
        } catch (Exception e) {
            System.err.println("Failed to create donation history: " + e.getMessage());
        }

        try {
            CreateRecipientHistoryRequest recipientHistoryRequest = buildRecipientHistoryRequest(matchResult, completedAt);
            recipientServiceClient.createRecipientHistory(recipientHistoryRequest);
            System.out.println("Recipient history created successfully");
        } catch (Exception e) {
            System.err.println("Failed to create recipient history: " + e.getMessage());
        }
        
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

    private CreateDonationHistoryRequest buildDonationHistoryRequest(MatchResult matchResult, LocalDateTime completedAt) {
        CreateDonationHistoryRequest request = new CreateDonationHistoryRequest();
        request.setMatchId(matchResult.getId());
        request.setReceiveRequestId(matchResult.getReceiveRequestId());
        request.setRecipientUserId(matchResult.getRecipientUserId());
        request.setMatchedAt(matchResult.getMatchedAt());
        request.setCompletedAt(completedAt);
        
        donationRepository.findById(matchResult.getDonationId())
                .ifPresent(donation -> populateDonationData(request, donation));
        
        donorRepository.findByUserId(matchResult.getDonorUserId())
                .ifPresent(donor -> populateDonorData(request, donor));
        
        donorRepository.findByUserId(matchResult.getDonorUserId())
                .ifPresent(donor -> donorHLAProfileRepository.findByDonorId(donor.getDonorId())
                        .ifPresent(hla -> populateDonorHLAData(request, hla)));
        
        return request;
    }

    private CreateRecipientHistoryRequest buildRecipientHistoryRequest(MatchResult matchResult, LocalDateTime completedAt) {
        CreateRecipientHistoryRequest request = new CreateRecipientHistoryRequest();
        request.setMatchId(matchResult.getId());
        request.setDonationId(matchResult.getDonationId());
        request.setDonorUserId(matchResult.getDonorUserId());
        request.setMatchedAt(matchResult.getMatchedAt());
        request.setCompletedAt(completedAt);
        
        receiveRequestRepository.findById(matchResult.getReceiveRequestId())
                .ifPresent(receiveRequest -> populateRequestData(request, receiveRequest));
        
        recipientRepository.findByUserId(matchResult.getRecipientUserId())
                .ifPresent(recipient -> populateRecipientData(request, recipient));
        
        recipientRepository.findByUserId(matchResult.getRecipientUserId())
                .ifPresent(recipient -> recipientHLAProfileRepository.findByRecipientId(recipient.getRecipientId())
                        .ifPresent(hla -> populateRecipientHLAData(request, hla)));
        
        return request;
    }
    
    private void populateDonationData(CreateDonationHistoryRequest request, Donation donation) {
        request.setDonationId(donation.getDonationId());
        request.setDonationDate(donation.getDonationDate());
        request.setDonationStatus(donation.getStatus().name());
        request.setBloodType(donation.getBloodType().name());
        request.setDonationType(donation.getDonationType().name());
    }
    
    private void populateDonorData(CreateDonationHistoryRequest request, Donor donor) {
        request.setDonorId(donor.getDonorId());
        request.setDonorUserId(donor.getUserId());
        request.setRegistrationDate(donor.getRegistrationDate());
        request.setDonorStatus(donor.getStatus() != null ? donor.getStatus().name() : null);
        
        request.setHemoglobinLevel(donor.getHemoglobinLevel());
        request.setBloodPressure(donor.getBloodPressure());
        request.setHasDiseases(donor.getHasDiseases());
        request.setTakingMedication(donor.getTakingMedication());
        request.setDiseaseDescription(donor.getDiseaseDescription());
        request.setCurrentMedications(donor.getCurrentMedications());
        request.setLastMedicalCheckup(donor.getLastMedicalCheckup());
        request.setMedicalHistory(donor.getMedicalHistory());
        request.setHasInfectiousDiseases(donor.getHasInfectiousDiseases());
        request.setInfectiousDiseaseDetails(donor.getInfectiousDiseaseDetails());
        request.setCreatinineLevel(donor.getCreatinineLevel());
        request.setLiverFunctionTests(donor.getLiverFunctionTests());
        request.setCardiacStatus(donor.getCardiacStatus());
        request.setPulmonaryFunction(donor.getPulmonaryFunction());
        request.setOverallHealthStatus(donor.getOverallHealthStatus());
        
        request.setWeight(donor.getWeight());
        request.setAge(donor.getAge());
        request.setDob(donor.getDob());
        request.setMedicalClearance(donor.getMedicalClearance());
        request.setRecentTattooOrPiercing(donor.getRecentTattooOrPiercing());
        request.setRecentTravelDetails(donor.getRecentTravelDetails());
        request.setRecentVaccination(donor.getRecentVaccination());
        request.setRecentSurgery(donor.getRecentSurgery());
        request.setChronicDiseases(donor.getChronicDiseases());
        request.setAllergies(donor.getAllergies());
        request.setLastDonationDate(donor.getLastDonationDate());
        request.setHeight(donor.getHeight());
        request.setBodyMassIndex(donor.getBodyMassIndex());
        request.setBodySize(donor.getBodySize());
        request.setIsLivingDonor(donor.getIsLivingDonor());
    }
    
    private void populateDonorHLAData(CreateDonationHistoryRequest request, DonorHLAProfile hla) {
        request.setHlaA1(hla.getHlaA1());
        request.setHlaA2(hla.getHlaA2());
        request.setHlaB1(hla.getHlaB1());
        request.setHlaB2(hla.getHlaB2());
        request.setHlaC1(hla.getHlaC1());
        request.setHlaC2(hla.getHlaC2());
        request.setHlaDR1(hla.getHlaDR1());
        request.setHlaDR2(hla.getHlaDR2());
        request.setHlaDQ1(hla.getHlaDQ1());
        request.setHlaDQ2(hla.getHlaDQ2());
        request.setHlaDP1(hla.getHlaDP1());
        request.setHlaDP2(hla.getHlaDP2());
        request.setTestingDate(hla.getTestingDate());
        request.setTestingMethod(hla.getTestingMethod());
        request.setLaboratoryName(hla.getLaboratoryName());
        request.setCertificationNumber(hla.getCertificationNumber());
        request.setHlaString(hla.getHlaString());
        request.setIsHighResolution(hla.getIsHighResolution());
    }
    
    private void populateRequestData(CreateRecipientHistoryRequest request, ReceiveRequest receiveRequest) {
        request.setReceiveRequestId(receiveRequest.getReceiveRequestId());
        request.setRequestType(receiveRequest.getRequestType().name());
        if (receiveRequest.getRequestedBloodType() != null) {
            request.setRequestedBloodType(receiveRequest.getRequestedBloodType().name());
        }
        if (receiveRequest.getRequestedOrgan() != null) {
            request.setRequestedOrgan(receiveRequest.getRequestedOrgan().name());
        }
        if (receiveRequest.getRequestedTissue() != null) {
            request.setRequestedTissue(receiveRequest.getRequestedTissue().name());
        }
        if (receiveRequest.getRequestedStemCellType() != null) {
            request.setRequestedStemCellType(receiveRequest.getRequestedStemCellType().name());
        }
        request.setUrgencyLevel(receiveRequest.getUrgencyLevel().name());
        request.setQuantity(receiveRequest.getQuantity());
        request.setRequestDate(receiveRequest.getRequestDate());
        request.setRequestStatus(receiveRequest.getStatus().name());
        request.setRequestNotes(receiveRequest.getNotes());
    }
    
    private void populateRecipientData(CreateRecipientHistoryRequest request, Recipient recipient) {
        request.setRecipientId(recipient.getRecipientId());
        request.setRecipientUserId(recipient.getUserId());
        request.setAvailability(recipient.getAvailability() != null ? recipient.getAvailability().name() : null);
        
        request.setDiagnosis(recipient.getDiagnosis());
        request.setAllergies(recipient.getAllergies());
        request.setCurrentMedications(recipient.getCurrentMedications());
        request.setAdditionalNotes(recipient.getAdditionalNotes());
        
        request.setMedicallyEligible(recipient.getMedicallyEligible());
        request.setLegalClearance(recipient.getLegalClearance());
        request.setNotes(recipient.getEligibilityNotes());
        request.setLastReviewed(recipient.getLastReviewed());
    }
    
    private void populateRecipientHLAData(CreateRecipientHistoryRequest request, RecipientHLAProfile hla) {
        request.setHlaA1(hla.getHlaA1());
        request.setHlaA2(hla.getHlaA2());
        request.setHlaB1(hla.getHlaB1());
        request.setHlaB2(hla.getHlaB2());
        request.setHlaC1(hla.getHlaC1());
        request.setHlaC2(hla.getHlaC2());
        request.setHlaDR1(hla.getHlaDR1());
        request.setHlaDR2(hla.getHlaDR2());
        request.setHlaDQ1(hla.getHlaDQ1());
        request.setHlaDQ2(hla.getHlaDQ2());
        request.setHlaDP1(hla.getHlaDP1());
        request.setHlaDP2(hla.getHlaDP2());
        request.setTestingDate(hla.getTestingDate());
        request.setTestingMethod(hla.getTestingMethod());
        request.setLaboratoryName(hla.getLaboratoryName());
        request.setCertificationNumber(hla.getCertificationNumber());
        request.setHlaString(hla.getHlaString());
        request.setIsHighResolution(hla.getIsHighResolution());
    }

}
