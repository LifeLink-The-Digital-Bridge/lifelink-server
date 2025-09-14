package com.matchingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchingservice.dto.*;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.model.MatchResult;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.repository.*;
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
                .ifPresent(donor -> {
                    populateDonorData(request, donor);
                    populateLocationData(request, donor);
                });

        donorRepository.findByUserId(matchResult.getDonorUserId())
                .ifPresent(donor -> donorHLAProfileRepository.findByDonorId(donor.getDonorId())
                        .ifPresent(hla -> populateDonorHLAData(request, hla)));

        return request;
    }

    private void populateDonationData(CreateDonationHistoryRequest request, Donation donation) {
        request.setDonationId(donation.getDonationId());
        request.setDonationDate(donation.getDonationDate());
        request.setDonationStatus(donation.getStatus().name());
        request.setBloodType(donation.getBloodType() != null ? donation.getBloodType().name() : null);
        request.setDonationType(donation.getDonationType().name());
        request.setUsedLocationId(donation.getLocationId());
        switch (donation.getDonationType()) {
            case BLOOD:
                if (donation instanceof BloodDonation) {
                    BloodDonation bloodDonation = (BloodDonation) donation;
                    request.setQuantity(bloodDonation.getQuantity());
                }
                break;

            case ORGAN:
                if (donation instanceof OrganDonation) {
                    OrganDonation organDonation = (OrganDonation) donation;
                    request.setOrganType(organDonation.getOrganType() != null ? organDonation.getOrganType().name() : null);
                    request.setIsCompatible(organDonation.getIsCompatible());
                    request.setOrganQuality(organDonation.getOrganQuality());
                    request.setOrganViabilityExpiry(organDonation.getOrganViabilityExpiry());
                    request.setColdIschemiaTime(organDonation.getColdIschemiaTime());
                    request.setOrganPerfused(organDonation.getOrganPerfused());
                    request.setOrganWeight(organDonation.getOrganWeight());
                    request.setOrganSize(organDonation.getOrganSize());
                    request.setFunctionalAssessment(organDonation.getFunctionalAssessment());
                    request.setHasAbnormalities(organDonation.getHasAbnormalities());
                    request.setAbnormalityDescription(organDonation.getAbnormalityDescription());
                }
                break;

            case TISSUE:
                if (donation instanceof TissueDonation) {
                    TissueDonation tissueDonation = (TissueDonation) donation;
                    request.setTissueType(tissueDonation.getTissueType() != null ? tissueDonation.getTissueType().name() : null);
                    request.setQuantity(tissueDonation.getQuantity());
                }
                break;

            case STEM_CELL:
                if (donation instanceof StemCellDonation) {
                    StemCellDonation stemCellDonation = (StemCellDonation) donation;
                    request.setStemCellType(stemCellDonation.getStemCellType() != null ? stemCellDonation.getStemCellType().name() : null);
                    request.setQuantity(stemCellDonation.getQuantity());
                }
                break;
        }
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


            request.setAge(donor.getAge());
            request.setDob(donor.getDob());
            request.setWeight(donor.getWeight());
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

    private void populateLocationData(CreateDonationHistoryRequest request, Donor donor) {
        if (request.getUsedLocationId() != null) {
            donorLocationRepository.findById(request.getUsedLocationId())
                    .ifPresent(location -> {
                        request.setUsedAddressLine(location.getAddressLine());
                        request.setUsedLandmark(location.getLandmark());
                        request.setUsedArea(location.getArea());
                        request.setUsedCity(location.getCity());
                        request.setUsedDistrict(location.getDistrict());
                        request.setUsedState(location.getState());
                        request.setUsedCountry(location.getCountry());
                        request.setUsedPincode(location.getPincode());
                        request.setUsedLatitude(location.getLatitude());
                        request.setUsedLongitude(location.getLongitude());
                    });
        }

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
                .ifPresent(recipient -> {
                    populateRecipientData(request, recipient);
                    populateRecipientLocationData(request, recipient);
                });

        recipientRepository.findByUserId(matchResult.getRecipientUserId())
                .ifPresent(recipient -> recipientHLAProfileRepository.findByRecipientId(recipient.getRecipientId())
                        .ifPresent(hla -> populateRecipientHLAData(request, hla)));

        return request;
    }

    private void populateRecipientLocationData(CreateRecipientHistoryRequest request, Recipient recipient) {
        if (request.getUsedLocationId() != null) {
            recipientLocationRepository.findById(request.getUsedLocationId())
                    .ifPresent(location -> {
                        request.setUsedAddressLine(location.getAddressLine());
                        request.setUsedLandmark(location.getLandmark());
                        request.setUsedArea(location.getArea());
                        request.setUsedCity(location.getCity());
                        request.setUsedDistrict(location.getDistrict());
                        request.setUsedState(location.getState());
                        request.setUsedCountry(location.getCountry());
                        request.setUsedPincode(location.getPincode());
                        request.setUsedLatitude(location.getLatitude());
                        request.setUsedLongitude(location.getLongitude());
                    });
        }
    }



    private void populateRequestData(CreateRecipientHistoryRequest request, ReceiveRequest receiveRequest) {
        request.setReceiveRequestId(receiveRequest.getReceiveRequestId());
        request.setRequestType(receiveRequest.getRequestType().name());
        request.setUsedLocationId(receiveRequest.getLocationId());

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

        request.setHemoglobinLevel(recipient.getHemoglobinLevel());
        request.setBloodPressure(recipient.getBloodPressure());
        request.setDiagnosis(recipient.getDiagnosis());
        request.setAllergies(recipient.getAllergies());
        request.setCurrentMedications(recipient.getCurrentMedications());
        request.setAdditionalNotes(recipient.getAdditionalNotes());
        request.setHasInfectiousDiseases(recipient.getHasInfectiousDiseases());
        request.setInfectiousDiseaseDetails(recipient.getInfectiousDiseaseDetails());
        request.setCreatinineLevel(recipient.getCreatinineLevel());
        request.setLiverFunctionTests(recipient.getLiverFunctionTests());
        request.setCardiacStatus(recipient.getCardiacStatus());
        request.setPulmonaryFunction(recipient.getPulmonaryFunction());
        request.setOverallHealthStatus(recipient.getOverallHealthStatus());

        request.setAgeEligible(recipient.getAgeEligible());
        request.setAge(recipient.getAge());
        request.setDob(recipient.getDob());
        request.setWeightEligible(recipient.getWeightEligible());
        request.setWeight(recipient.getWeight());
        request.setMedicallyEligible(recipient.getMedicallyEligible());
        request.setLegalClearance(recipient.getLegalClearance());
        request.setNotes(recipient.getEligibilityNotes());
        request.setLastReviewed(recipient.getLastReviewed());
        request.setHeight(recipient.getHeight());
        request.setBodyMassIndex(recipient.getBodyMassIndex());
        request.setBodySize(recipient.getBodySize());
        request.setIsLivingDonor(recipient.getIsLivingDonor());
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

    public boolean hasAccessToDonation(UUID donationId, UUID recipientUserId) {
        List<MatchResult> matchResults = matchResultRepository.findByDonationIdAndRecipientUserId(donationId, recipientUserId);
        System.out.println("Match results for donation " + donationId + " and recipient " + recipientUserId + ": " + matchResults.toString());

        return !matchResults.isEmpty();
    }

    public boolean hasAccessToRequest(UUID requestId, UUID donorUserId) {
        List<MatchResult> matchResults = matchResultRepository.findByReceiveRequestIdAndDonorUserId(requestId, donorUserId);
        System.out.println("Match results for request " + requestId + " and donor " + donorUserId + ": " + matchResults.toString());

        return !matchResults.isEmpty();
    }


    public DonationDTO getDonationById(UUID donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

        return convertToDTO(donation);
    }

    public ReceiveRequestDTO getRequestById(UUID requestId) {
        ReceiveRequest request = receiveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        return convertToDTO(request);
    }
    private DonationDTO convertToDTO(Donation donation) {
        DonationDTO dto = new DonationDTO();
        dto.setId(donation.getDonationId());
        dto.setDonorId(donation.getDonorId());
        dto.setLocationId(donation.getLocationId());
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

        return dto;
    }
    private ReceiveRequestDTO convertToDTO(ReceiveRequest receiveRequest) {
        ReceiveRequestDTO dto = new ReceiveRequestDTO();
        dto.setId(receiveRequest.getReceiveRequestId());
        dto.setRecipientId(receiveRequest.getRecipientId());
        dto.setLocationId(receiveRequest.getLocationId());
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

        return dto;
    }


}
