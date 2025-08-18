package com.matchingservice.service;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.model.Donation;
import com.matchingservice.model.DonorLocation;
import com.matchingservice.model.MatchResult;
import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.model.recipients.RecipientLocation;
import com.matchingservice.repository.DonationRepository;
import com.matchingservice.repository.DonorLocationRepository;
import com.matchingservice.repository.MatchResultRepository;
import com.matchingservice.repository.ReceiveRequestRepository;
import com.matchingservice.repository.RecipientLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchResultRepository matchResultRepository;
    private final DonationRepository donationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final RecipientLocationRepository recipientLocationRepository;

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
                donorLocation = donorLocationRepository.findByLocationId(request.getDonorLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Donor location not found with ID: " + request.getDonorLocationId()));
            } else if (donation.getLocation() != null) {
                donorLocation = donation.getLocation();
            }
            RecipientLocation recipientLocation = null;
            if (request.getRecipientLocationId() != null) {
                recipientLocation = recipientLocationRepository.findById(request.getRecipientLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Recipient location not found with ID: " + request.getRecipientLocationId()));
            } else if (receiveRequest.getLocation() != null) {
                recipientLocation = receiveRequest.getLocation();
            }

            MatchResult matchResult = new MatchResult();
            matchResult.setDonation(donation);
            matchResult.setReceiveRequest(receiveRequest);

            matchResult.setMatchedAt(LocalDateTime.now());
            matchResult.setIsConfirmed(false);

            matchResult.setDonorLocation(donorLocation);
            matchResult.setRecipientLocation(recipientLocation);
            matchResult.setDistance(0.0);

            MatchResult savedMatchResult = matchResultRepository.save(matchResult);

            System.out.println("Successfully created manual match result: " + savedMatchResult.getId());

            return new ManualMatchResponse(
                    true,
                    "Manual match successful. MatchResult ID: " + savedMatchResult.getId(),
                    savedMatchResult.getId()
            );
        } catch (ResourceNotFoundException e) {
            System.err.println("Validation Error during manual match: " + e.getMessage());
            return new ManualMatchResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Error during manual match: " + e.getMessage());
            e.printStackTrace();
            return new ManualMatchResponse(false, "Failed to perform manual match: " + e.getMessage(), null);
        }
    }
}
