package com.matchingservice.service;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;
import com.matchingservice.dto.MatchResponse;
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
}
