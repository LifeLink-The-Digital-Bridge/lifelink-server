package com.matchingservice.service;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;
import com.matchingservice.dto.MatchResponse;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.model.donor.Donation;
import com.matchingservice.model.donor.DonorLocation;
import com.matchingservice.model.donor.MatchResult;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.repository.DonationRepository;
import com.matchingservice.repository.DonorLocationRepository;
import com.matchingservice.repository.MatchResultRepository;
import com.matchingservice.repository.ReceiveRequestRepository;
import com.matchingservice.repository.RecipientLocationRepository;
import com.matchingservice.repository.RecipientRepository;
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
    public List<MatchResponse> getMatchesByDonation(UUID donationId) {
        return matchResultRepository.findByDonationId(donationId)
                .stream()
                .map(this::enrichMatchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesByRequest(UUID receiveRequestId) {
        return matchResultRepository.findByReceiveRequestId(receiveRequestId)
                .stream()
                .map(this::enrichMatchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MatchResponse getMatchById(UUID matchId) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));
        return enrichMatchResponse(matchResult);
    }

    private MatchResponse enrichMatchResponse(MatchResult matchResult) {
        MatchResponse response = MatchResponse.fromMatchResult(matchResult);
        
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

    @Override
    public List<MatchResponse> getMatchesForDonor(UUID donorUserId) {
        return matchResultRepository.findByDonorUserId(donorUserId)
                .stream()
                .map(this::enrichMatchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesForRecipient(UUID recipientUserId) {
        return matchResultRepository.findByRecipientUserId(recipientUserId)
                .stream()
                .map(this::enrichMatchResponse)
                .collect(Collectors.toList());
    }
}
