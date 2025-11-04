package com.matchingservice.service;

import com.matchingservice.client.DonorServiceClient;
import com.matchingservice.client.RecipientServiceClient;
import com.matchingservice.dto.*;
import com.matchingservice.enums.*;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.model.MatchResult;
import com.matchingservice.model.donor.*;
import com.matchingservice.model.recipients.*;
import com.matchingservice.repository.*;
import com.matchingservice.repository.donor.*;
import com.matchingservice.repository.recipient.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceImpl implements MatchingService {

    private final MatchResultRepository matchResultRepository;
    private final DonationRepository donationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final DonorRepository donorRepository;
    private final RecipientRepository recipientRepository;
    private final DonorLocationRepository donorLocationRepository;
    private final RecipientLocationRepository recipientLocationRepository;
    private final DonorHLAProfileRepository donorHLAProfileRepository;
    private final RecipientHLAProfileRepository recipientHLAProfileRepository;

    private final DonorServiceClient donorServiceClient;
    private final RecipientServiceClient recipientServiceClient;

    @Override
    @Transactional
    public String donorConfirmMatch(UUID matchId, UUID userId) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getDonorUserId().equals(userId)) {
            throw new IllegalStateException("Only the donor of this match can confirm");
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot confirm a completed match");
        }

        if (match.getStatus() == MatchStatus.REJECTED) {
            throw new IllegalStateException("This match has been rejected and cannot be confirmed");
        }

        if (match.getStatus() == MatchStatus.EXPIRED) {
            throw new IllegalStateException("This match has expired and cannot be confirmed");
        }

        if (match.getStatus() == MatchStatus.CANCELLED_BY_DONOR ||
                match.getStatus() == MatchStatus.CANCELLED_BY_RECIPIENT) {
            throw new IllegalStateException("This match has been cancelled");
        }

        if (match.getStatus() == MatchStatus.WITHDRAWN) {
            if (match.getWithdrawnBy() != ConfirmerType.DONOR) {
                throw new IllegalStateException("Cannot re-confirm: withdrawn by other party");
            }

            LocalDateTime withdrawnAt = match.getWithdrawnAt();
            if (withdrawnAt == null || LocalDateTime.now().isAfter(withdrawnAt.plusHours(2))) {
                throw new IllegalStateException(
                        "Re-confirmation window (2 hours) has expired. Match status: WITHDRAWN"
                );
            }

            log.info("Donor re-confirming withdrawn match {}", matchId);
            match.setDonorConfirmed(true);
            match.setDonorConfirmedAt(LocalDateTime.now());

            if (match.getRecipientConfirmed()) {
                match.setStatus(MatchStatus.CONFIRMED);
                match.setIsConfirmed(true);
                match.setWithdrawnBy(null);
                match.setWithdrawnAt(null);
                match.setWithdrawalReason(null);

                updateDonationStatus(match.getDonationId(), DonationStatus.IN_PROGRESS);
                updateRequestStatus(match.getReceiveRequestId(), RequestStatus.IN_PROGRESS);
                expireOtherMatchesForConfirmedMatch(match);

                matchResultRepository.save(match);

                return "Match re-confirmed successfully! Both parties have confirmed. " +
                        "You have 2 hours to withdraw if needed.";
            } else {
                match.setStatus(MatchStatus.DONOR_CONFIRMED);
                match.setFirstConfirmer(ConfirmerType.DONOR);
                match.setFirstConfirmedAt(LocalDateTime.now());
                match.setConfirmationExpiresAt(LocalDateTime.now().plusHours(48));
                match.setWithdrawnBy(null);
                match.setWithdrawnAt(null);
                match.setWithdrawalReason(null);

                matchResultRepository.save(match);

                return "Match re-confirmed successfully! Waiting for recipient to confirm within 48 hours.";
            }
        }

        if (match.getIsConfirmed()) {
            throw new IllegalStateException("This match has already been confirmed by both parties");
        }

        match.setDonorConfirmed(true);
        match.setDonorConfirmedAt(LocalDateTime.now());

        if (match.getRecipientConfirmed()) {
            match.setStatus(MatchStatus.CONFIRMED);
            match.setIsConfirmed(true);

            updateDonationStatus(match.getDonationId(), DonationStatus.IN_PROGRESS);
            updateRequestStatus(match.getReceiveRequestId(), RequestStatus.IN_PROGRESS);
            expireOtherMatchesForConfirmedMatch(match);

            matchResultRepository.save(match);

            return "Congratulations! Both parties have confirmed this match. " +
                    "The donation process can now proceed. You have 2 hours to withdraw if needed.";
        } else {
            match.setStatus(MatchStatus.DONOR_CONFIRMED);
            match.setFirstConfirmer(ConfirmerType.DONOR);
            match.setFirstConfirmedAt(LocalDateTime.now());
            match.setConfirmationExpiresAt(LocalDateTime.now().plusHours(48));

            matchResultRepository.save(match);

            return "Thank you for confirming! We're now waiting for the recipient to confirm within 48 hours. " +
                    "You'll be notified once they respond.";
        }
    }

    @Override
    @Transactional
    public String recipientConfirmMatch(UUID matchId, UUID userId) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getRecipientUserId().equals(userId)) {
            throw new IllegalStateException("Only the recipient of this match can confirm");
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot confirm a completed match");
        }

        if (match.getStatus() == MatchStatus.REJECTED) {
            throw new IllegalStateException("This match has been rejected and cannot be confirmed");
        }

        if (match.getStatus() == MatchStatus.EXPIRED) {
            throw new IllegalStateException("This match has expired and cannot be confirmed");
        }

        if (match.getStatus() == MatchStatus.CANCELLED_BY_DONOR ||
                match.getStatus() == MatchStatus.CANCELLED_BY_RECIPIENT) {
            throw new IllegalStateException("This match has been cancelled");
        }

        if (match.getStatus() == MatchStatus.WITHDRAWN) {
            if (match.getWithdrawnBy() != ConfirmerType.RECIPIENT) {
                throw new IllegalStateException("Cannot re-confirm: withdrawn by other party");
            }

            LocalDateTime withdrawnAt = match.getWithdrawnAt();
            if (withdrawnAt == null || LocalDateTime.now().isAfter(withdrawnAt.plusHours(2))) {
                throw new IllegalStateException(
                        "Re-confirmation window (2 hours) has expired. Match status: WITHDRAWN"
                );
            }

            log.info("Recipient re-confirming withdrawn match {}", matchId);
            match.setRecipientConfirmed(true);
            match.setRecipientConfirmedAt(LocalDateTime.now());

            if (match.getDonorConfirmed()) {
                match.setStatus(MatchStatus.CONFIRMED);
                match.setIsConfirmed(true);
                match.setWithdrawnBy(null);
                match.setWithdrawnAt(null);
                match.setWithdrawalReason(null);

                updateDonationStatus(match.getDonationId(), DonationStatus.IN_PROGRESS);
                updateRequestStatus(match.getReceiveRequestId(), RequestStatus.IN_PROGRESS);
                expireOtherMatchesForConfirmedMatch(match);

                matchResultRepository.save(match);

                return "Match re-confirmed successfully! Both parties have confirmed. " +
                        "You have 2 hours to withdraw if needed.";
            } else {
                match.setStatus(MatchStatus.RECIPIENT_CONFIRMED);
                match.setFirstConfirmer(ConfirmerType.RECIPIENT);
                match.setFirstConfirmedAt(LocalDateTime.now());
                match.setConfirmationExpiresAt(LocalDateTime.now().plusHours(48));
                match.setWithdrawnBy(null);
                match.setWithdrawnAt(null);
                match.setWithdrawalReason(null);

                matchResultRepository.save(match);

                return "Match re-confirmed successfully! Waiting for donor to confirm within 48 hours.";
            }
        }

        if (match.getIsConfirmed()) {
            throw new IllegalStateException("This match has already been confirmed by both parties");
        }

        match.setRecipientConfirmed(true);
        match.setRecipientConfirmedAt(LocalDateTime.now());

        if (match.getDonorConfirmed()) {
            match.setStatus(MatchStatus.CONFIRMED);
            match.setIsConfirmed(true);

            updateDonationStatus(match.getDonationId(), DonationStatus.IN_PROGRESS);
            updateRequestStatus(match.getReceiveRequestId(), RequestStatus.IN_PROGRESS);
            expireOtherMatchesForConfirmedMatch(match);

            matchResultRepository.save(match);

            return "Congratulations! Both parties have confirmed this match. " +
                    "The donation process can now proceed. You have 2 hours to withdraw if needed.";
        } else {
            match.setStatus(MatchStatus.RECIPIENT_CONFIRMED);
            match.setFirstConfirmer(ConfirmerType.RECIPIENT);
            match.setFirstConfirmedAt(LocalDateTime.now());
            match.setConfirmationExpiresAt(LocalDateTime.now().plusHours(48));

            matchResultRepository.save(match);

            return "Thank you for confirming! We're now waiting for the donor to confirm within 48 hours. " +
                    "You'll be notified once they respond.";
        }
    }

    @Override
    @Transactional
    public String donorRejectMatch(UUID matchId, UUID userId, String reason) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getDonorUserId().equals(userId)) {
            throw new IllegalStateException("Only the donor of this match can reject it");
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reject a completed match");
        }

        if (match.getStatus() == MatchStatus.CONFIRMED) {
            LocalDateTime confirmedAt = match.getDonorConfirmedAt();
            if (confirmedAt != null && LocalDateTime.now().isAfter(confirmedAt.plusHours(2))) {
                throw new IllegalStateException(
                        "This match is fully confirmed and the 2-hour grace period has passed. " +
                                "Please contact support for assistance."
                );
            }
        }

        if (match.getStatus() == MatchStatus.REJECTED) {
            throw new IllegalStateException("This match has already been rejected");
        }

        if (match.getStatus() == MatchStatus.EXPIRED) {
            throw new IllegalStateException("This match has already expired");
        }

        if (match.getStatus() == MatchStatus.CANCELLED_BY_DONOR ||
                match.getStatus() == MatchStatus.CANCELLED_BY_RECIPIENT) {
            throw new IllegalStateException("Cannot reject a cancelled match");
        }

        match.setStatus(MatchStatus.REJECTED);
        match.setExpiryReason("Rejected by donor: " + reason);
        match.setExpiredAt(LocalDateTime.now());

        matchResultRepository.save(match);

        checkAndResetToPending(match.getDonationId(), match.getReceiveRequestId());

        return "Match rejected successfully. The recipient has been notified. " +
                "You cannot re-confirm this match.";
    }

    @Override
    @Transactional
    public String recipientRejectMatch(UUID matchId, UUID userId, String reason) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getRecipientUserId().equals(userId)) {
            throw new IllegalStateException("Only the recipient of this match can reject it");
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reject a completed match");
        }

        if (match.getStatus() == MatchStatus.CONFIRMED) {
            LocalDateTime confirmedAt = match.getRecipientConfirmedAt();
            if (confirmedAt != null && LocalDateTime.now().isAfter(confirmedAt.plusHours(2))) {
                throw new IllegalStateException(
                        "This match is fully confirmed and the 2-hour grace period has passed. " +
                                "Please contact support for assistance."
                );
            }
        }

        if (match.getStatus() == MatchStatus.REJECTED) {
            throw new IllegalStateException("This match has already been rejected");
        }

        if (match.getStatus() == MatchStatus.EXPIRED) {
            throw new IllegalStateException("This match has already expired");
        }

        if (match.getStatus() == MatchStatus.CANCELLED_BY_DONOR ||
                match.getStatus() == MatchStatus.CANCELLED_BY_RECIPIENT) {
            throw new IllegalStateException("Cannot reject a cancelled match");
        }

        match.setStatus(MatchStatus.REJECTED);
        match.setExpiryReason("Rejected by recipient: " + reason);
        match.setExpiredAt(LocalDateTime.now());

        matchResultRepository.save(match);

        checkAndResetToPending(match.getDonationId(), match.getReceiveRequestId());

        return "Match rejected successfully. The donor has been notified. " +
                "You cannot re-confirm this match.";
    }

    @Override
    @Transactional
    public String donorWithdrawConfirmation(UUID matchId, UUID userId, String reason) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getDonorUserId().equals(userId)) {
            throw new IllegalStateException("Only the donor of this match can withdraw");
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot withdraw a completed match");
        }

        if (match.getStatus() != MatchStatus.CONFIRMED &&
                match.getStatus() != MatchStatus.DONOR_CONFIRMED) {
            throw new IllegalStateException("Can only withdraw after confirming");
        }

        LocalDateTime confirmedAt = match.getDonorConfirmedAt();
        if (confirmedAt == null || LocalDateTime.now().isAfter(confirmedAt.plusHours(2))) {
            throw new IllegalStateException(
                    "Withdrawal grace period (2 hours) has expired. " +
                            "Please contact support for assistance."
            );
        }

        boolean wasBothConfirmed = match.getStatus() == MatchStatus.CONFIRMED;

        match.setStatus(MatchStatus.WITHDRAWN);
        match.setWithdrawnBy(ConfirmerType.DONOR);
        match.setWithdrawnAt(LocalDateTime.now());
        match.setWithdrawalReason(reason);
        match.setDonorConfirmed(false);
        match.setIsConfirmed(false);

        matchResultRepository.save(match);

        if (wasBothConfirmed) {
            updateDonationStatus(match.getDonationId(), DonationStatus.MATCHED);
            updateRequestStatus(match.getReceiveRequestId(), RequestStatus.MATCHED);
        }

        return "Confirmation withdrawn successfully. " +
                "You can re-confirm this match within 2 hours if you change your mind.";
    }

    @Override
    @Transactional
    public String recipientWithdrawConfirmation(UUID matchId, UUID userId, String reason) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getRecipientUserId().equals(userId)) {
            throw new IllegalStateException("Only the recipient of this match can withdraw");
        }

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalStateException("Cannot withdraw a completed match");
        }

        if (match.getStatus() != MatchStatus.CONFIRMED &&
                match.getStatus() != MatchStatus.RECIPIENT_CONFIRMED) {
            throw new IllegalStateException("Can only withdraw after confirming");
        }

        LocalDateTime confirmedAt = match.getRecipientConfirmedAt();
        if (confirmedAt == null || LocalDateTime.now().isAfter(confirmedAt.plusHours(2))) {
            throw new IllegalStateException(
                    "Withdrawal grace period (2 hours) has expired. " +
                            "Please contact support for assistance."
            );
        }

        boolean wasBothConfirmed = match.getStatus() == MatchStatus.CONFIRMED;

        match.setStatus(MatchStatus.WITHDRAWN);
        match.setWithdrawnBy(ConfirmerType.RECIPIENT);
        match.setWithdrawnAt(LocalDateTime.now());
        match.setWithdrawalReason(reason);
        match.setRecipientConfirmed(false);
        match.setIsConfirmed(false);

        matchResultRepository.save(match);

        if (wasBothConfirmed) {
            updateDonationStatus(match.getDonationId(), DonationStatus.MATCHED);
            updateRequestStatus(match.getReceiveRequestId(), RequestStatus.MATCHED);
        }

        return "Confirmation withdrawn successfully. " +
                "You can re-confirm this match within 2 hours if you change your mind.";
    }

    @Override
    @Transactional
    public String recipientConfirmCompletion(UUID matchId, UUID userId, CompletionConfirmationDTO details) {
        MatchResult match = matchResultRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID " + matchId));

        if (!match.getRecipientUserId().equals(userId)) {
            throw new IllegalStateException("Only the recipient of this match can confirm completion");
        }

        if (match.getStatus() != MatchStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Can only confirm completion for confirmed matches. Current status: " + match.getStatus()
            );
        }

        if (match.getCompletedAt() != null) {
            throw new IllegalStateException(
                    "This match has already been marked as completed on " + match.getCompletedAt()
            );
        }

        match.setStatus(MatchStatus.COMPLETED);
        match.setCompletedAt(LocalDateTime.now());
        match.setCompletionConfirmedBy(userId);
        match.setCompletionNotes(details.getNotes());
        match.setReceivedDate(details.getReceivedDate());
        match.setRecipientRating(details.getRating());
        match.setHospitalName(details.getHospitalName());

        matchResultRepository.save(match);

        updateDonationStatus(match.getDonationId(), DonationStatus.COMPLETED);
        updateRequestStatus(match.getReceiveRequestId(), RequestStatus.FULFILLED);

        return "Thank you for confirming! The donation has been marked as successfully completed. " +
                "The donor will be notified of your confirmation" +
                (details.getRating() != null ? " and rating." : ".");
    }

    private void updateDonationStatus(UUID donationId, DonationStatus newStatus) {
        donationRepository.findById(donationId).ifPresent(donation -> {
            donation.setStatus(newStatus);

            if (newStatus == DonationStatus.COMPLETED) {
                donation.setCompletedAt(LocalDateTime.now());
            }

            donationRepository.save(donation);
        });

        try {
            donorServiceClient.updateDonationStatus(donationId, newStatus);
        } catch (Exception e) {
            log.error("Failed to sync with donor-service: {}", e.getMessage());
        }
    }

    private void updateRequestStatus(UUID requestId, RequestStatus newStatus) {
        receiveRequestRepository.findById(requestId).ifPresent(request -> {
            request.setStatus(newStatus);

            if (newStatus == RequestStatus.FULFILLED) {
                request.setFulfilledAt(LocalDateTime.now());
            }

            receiveRequestRepository.save(request);
        });

        try {
            recipientServiceClient.updateRequestStatus(requestId, newStatus);
        } catch (Exception e) {
            log.error("Failed to sync with recipient-service: {}", e.getMessage());
        }
    }

    private void expireOtherMatchesForConfirmedMatch(MatchResult confirmedMatch) {
        matchResultRepository.expirePendingDonationMatches(
                confirmedMatch.getDonationId(),
                confirmedMatch.getId()
        );

        matchResultRepository.expirePendingRequestMatches(
                confirmedMatch.getReceiveRequestId(),
                confirmedMatch.getId()
        );
    }

    private void checkAndResetToPending(UUID donationId, UUID requestId) {
        List<MatchResult> donationMatches = matchResultRepository
                .findByDonationIdAndStatusIn(
                        donationId,
                        List.of(MatchStatus.PENDING, MatchStatus.DONOR_CONFIRMED,
                                MatchStatus.RECIPIENT_CONFIRMED, MatchStatus.CONFIRMED)
                );

        if (donationMatches.isEmpty()) {
            updateDonationStatus(donationId, DonationStatus.PENDING);
        }

        List<MatchResult> requestMatches = matchResultRepository
                .findByReceiveRequestIdAndStatusIn(
                        requestId,
                        List.of(MatchStatus.PENDING, MatchStatus.DONOR_CONFIRMED,
                                MatchStatus.RECIPIENT_CONFIRMED, MatchStatus.CONFIRMED)
                );

        if (requestMatches.isEmpty()) {
            updateRequestStatus(requestId, RequestStatus.PENDING);
        }
    }

    @Override
    public List<MatchResponse> getMatchesForDonor(UUID userId) {
        log.info("Fetching donor matches for userId: {}", userId);
        List<MatchResult> matches = matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(userId);
        log.info("Found {} matches for donor userId: {}", matches.size(), userId);
        return matches.stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesForRecipient(UUID userId) {
        return matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(userId)
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

    @Override
    public List<MatchResponse> getMatchesByDonation(UUID donationId) {
        return matchResultRepository.findByDonationIdOrderByCompatibilityScoreDesc(donationId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "DONOR_TO_RECIPIENT"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesByRequest(UUID requestId) {
        return matchResultRepository.findByReceiveRequestIdOrderByCompatibilityScoreDesc(requestId)
                .stream()
                .map(matchResult -> enrichMatchResponse(matchResult, "RECIPIENT_TO_DONOR"))
                .collect(Collectors.toList());
    }

    private MatchResponse enrichMatchResponse(MatchResult matchResult, String matchType) {
        MatchResponse response = MatchResponse.fromMatchResult(matchResult);
        response.setMatchType(matchType);

        donationRepository.findById(matchResult.getDonationId()).ifPresent(donation -> {
            response.setDonationType(donation.getDonationType() != null ? donation.getDonationType().toString() : null);
            response.setBloodType(donation.getBloodType() != null ? donation.getBloodType().toString() : null);
        });

        receiveRequestRepository.findById(matchResult.getReceiveRequestId()).ifPresent(request -> {
            response.setRequestType(request.getRequestType() != null ? request.getRequestType().toString() : null);
        });

        return response;
    }


    @Override
    public boolean isMatchConfirmed(UUID matchId) {
        return matchResultRepository.findById(matchId)
                .map(MatchResult::getIsConfirmed)
                .orElse(false);
    }

    @Override
    public List<MatchResponse> getActiveMatchesForUser(UUID userId) {
        List<MatchResult> donorMatches = matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(userId);
        List<MatchResult> recipientMatches = matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(userId);

        List<MatchResult> allMatches = new ArrayList<>();
        allMatches.addAll(donorMatches);
        allMatches.addAll(recipientMatches);

        return allMatches.stream()
                .filter(match -> match.getStatus() != MatchStatus.REJECTED &&
                        match.getStatus() != MatchStatus.EXPIRED &&
                        match.getStatus() != MatchStatus.COMPLETED &&
                        match.getStatus() != MatchStatus.CANCELLED_BY_DONOR &&
                        match.getStatus() != MatchStatus.CANCELLED_BY_RECIPIENT)
                .map(m -> enrichMatchResponse(m, "COMBINED"))
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getPendingMatchesForUser(UUID userId) {
        List<MatchResult> donorMatches = matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(userId);
        List<MatchResult> recipientMatches = matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(userId);

        List<MatchResult> allMatches = new ArrayList<>();
        allMatches.addAll(donorMatches);
        allMatches.addAll(recipientMatches);

        return allMatches.stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING ||
                        match.getStatus() == MatchStatus.DONOR_CONFIRMED ||
                        match.getStatus() == MatchStatus.RECIPIENT_CONFIRMED ||
                        match.getStatus() == MatchStatus.WITHDRAWN)
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getConfirmedMatchesForUser(UUID userId) {
        List<MatchResult> donorMatches = matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(userId);
        List<MatchResult> recipientMatches = matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(userId);

        List<MatchResult> allMatches = new ArrayList<>();
        allMatches.addAll(donorMatches);
        allMatches.addAll(recipientMatches);

        return allMatches.stream()
                .filter(match -> match.getStatus() == MatchStatus.CONFIRMED)
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> canConfirmCompletion(UUID matchId, UUID userId) {
        MatchResult match = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        Map<String, Object> result = new HashMap<>();

        boolean canConfirm = match.getRecipientUserId().equals(userId) &&
                match.getStatus() == MatchStatus.CONFIRMED &&
                match.getCompletedAt() == null;

        result.put("canConfirm", canConfirm);
        result.put("matchStatus", match.getStatus().name());
        result.put("isConfirmed", match.getIsConfirmed());
        result.put("alreadyCompleted", match.getCompletedAt() != null);

        if (!canConfirm) {
            if (!match.getRecipientUserId().equals(userId)) {
                result.put("reason", "Only the recipient can confirm completion");
            } else if (match.getStatus() != MatchStatus.CONFIRMED) {
                result.put("reason", "Match must be confirmed by both parties first");
            } else if (match.getCompletedAt() != null) {
                result.put("reason", "Match already completed");
            }
        }

        return result;
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
    public boolean hasAccessToDonation(UUID donationId, UUID userId) {
        return donationRepository.findById(donationId)
                .map(donation -> donation.getDonor().getUserId().equals(userId))
                .orElse(false) || matchResultRepository.existsByDonationIdAndRecipientUserId(donationId, userId);
    }

    @Override
    public boolean hasAccessToRequest(UUID requestId, UUID userId) {
        return receiveRequestRepository.findById(requestId)
                .map(request -> request.getRecipient().getUserId().equals(userId))
                .orElse(false) || matchResultRepository.existsByReceiveRequestIdAndDonorUserId(requestId, userId);
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
    public DonorDTO getDonorSnapshotByDonation(UUID donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + donationId));

        Donor historicalDonor = donation.getDonor();
        return convertDonorToDTO(historicalDonor);
    }

    @Override
    public RecipientDTO getRecipientSnapshotByRequest(UUID requestId) {
        ReceiveRequest request = receiveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        Recipient historicalRecipient = request.getRecipient();
        return convertRecipientToDTO(historicalRecipient);
    }

    @Override
    @Transactional
    public ManualMatchResponse manualMatch(ManualMatchRequest request) {
        try {
            Donation donation = donationRepository.findById(request.getDonationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));

            ReceiveRequest receiveRequest = receiveRequestRepository.findById(request.getReceiveRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

            String compatibilityError = validateCompatibility(donation, receiveRequest);
            if (compatibilityError != null) {
                return buildErrorResponse("INCOMPATIBLE", compatibilityError);
            }

            boolean matchExists = matchResultRepository.existsByDonationIdAndReceiveRequestId(
                    donation.getDonationId(),
                    receiveRequest.getReceiveRequestId()
            );

            if (matchExists) {
                return buildErrorResponse("DUPLICATE", "Match already exists between this donation and request");
            }

            MatchResult matchResult = createMatchResult(donation, receiveRequest);
            MatchResult savedMatchResult = matchResultRepository.save(matchResult);

            updateDonationStatus(donation.getDonationId(), DonationStatus.MATCHED);
            updateRequestStatus(receiveRequest.getReceiveRequestId(), RequestStatus.MATCHED);

            return buildSuccessResponse(savedMatchResult, donation, receiveRequest);

        } catch (ResourceNotFoundException e) {
            return buildErrorResponse("NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("Error creating manual match: {}", e.getMessage(), e);
            return buildErrorResponse("ERROR", e.getMessage());
        }
    }

    private MatchResult createMatchResult(Donation donation, ReceiveRequest request) {
        MatchResult match = new MatchResult();
        match.setDonationId(donation.getDonationId());
        match.setReceiveRequestId(request.getReceiveRequestId());
        match.setDonorUserId(donation.getUserId());
        match.setRecipientUserId(request.getUserId());
        match.setDonorLocationId(donation.getLocation() != null ? donation.getLocation().getLocationId() : null);
        match.setRecipientLocationId(request.getLocation() != null ? request.getLocation().getLocationId() : null);

        if (donation.getLocation() != null && request.getLocation() != null) {
            double distance = calculateDistance(
                    donation.getLocation().getLatitude(),
                    donation.getLocation().getLongitude(),
                    request.getLocation().getLatitude(),
                    request.getLocation().getLongitude()
            );
            match.setDistance(distance);
        }

        match.setStatus(MatchStatus.PENDING);
        match.setMatchedAt(LocalDateTime.now());
        match.setIsConfirmed(false);
        match.setDonorConfirmed(false);
        match.setRecipientConfirmed(false);
        match.setCompatibilityScore(1.0);
        match.setMatchReason("Manual match by admin");

        return match;
    }

    private ManualMatchResponse buildSuccessResponse(MatchResult match, Donation donation, ReceiveRequest request) {
        ManualMatchResponse.MatchDetails details = ManualMatchResponse.MatchDetails.builder()
                .donationId(match.getDonationId())
                .receiveRequestId(match.getReceiveRequestId())
                .donorUserId(match.getDonorUserId())
                .recipientUserId(match.getRecipientUserId())
                .donationType(donation.getDonationType().name())
                .requestType(request.getRequestType().name())
                .bloodType(donation.getBloodType() != null ? donation.getBloodType().name() : null)
                .matchType("MANUAL")
                .matchedAt(match.getMatchedAt())
                .status(match.getStatus().name())
                .distance(match.getDistance())
                .build();

        return ManualMatchResponse.builder()
                .success(true)
                .matchResultId(match.getId())
                .message("Match created successfully")
                .matchDetails(details)
                .build();
    }

    private ManualMatchResponse buildErrorResponse(String errorType, String message) {
        ManualMatchResponse.ErrorDetails error = ManualMatchResponse.ErrorDetails.builder()
                .errorType(errorType)
                .errorMessage(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ManualMatchResponse.builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0.0;
        }
        final double R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    private String validateCompatibility(Donation donation, ReceiveRequest request) {
        if (donation.getUserId().equals(request.getUserId())) {
            return "Cannot match donation and request from the same user";
        }

        if (!donation.getDonationType().toString().equals(request.getRequestType().toString())) {
            return "Donation type (" + donation.getDonationType() + ") does not match request type (" + request.getRequestType() + ")";
        }

        if (donation.getStatus() != DonationStatus.PENDING && donation.getStatus() != DonationStatus.MATCHED) {
            return "Donation must be in PENDING or MATCHED status, current status: " + donation.getStatus();
        }

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.MATCHED) {
            return "Request must be in PENDING or MATCHED status, current status: " + request.getStatus();
        }

        if (donation.getBloodType() != null && request.getRequestedBloodType() != null) {
            if (!donation.getBloodType().equals(request.getRequestedBloodType())) {
                return "Blood type mismatch: Donation is " + donation.getBloodType() + ", Request needs " + request.getRequestedBloodType();
            }
        }

        if (donation instanceof OrganDonation && request.getRequestedOrgan() != null) {
            OrganDonation organ = (OrganDonation) donation;
            if (!organ.getOrganType().equals(request.getRequestedOrgan())) {
                return "Organ type mismatch: Donation is " + organ.getOrganType() + ", Request needs " + request.getRequestedOrgan();
            }
        }

        if (donation instanceof TissueDonation && request.getRequestedTissue() != null) {
            TissueDonation tissue = (TissueDonation) donation;
            if (!tissue.getTissueType().equals(request.getRequestedTissue())) {
                return "Tissue type mismatch: Donation is " + tissue.getTissueType() + ", Request needs " + request.getRequestedTissue();
            }
        }

        if (donation instanceof StemCellDonation && request.getRequestedStemCellType() != null) {
            StemCellDonation stemCell = (StemCellDonation) donation;
            if (!stemCell.getStemCellType().equals(request.getRequestedStemCellType())) {
                return "Stem cell type mismatch: Donation is " + stemCell.getStemCellType() + ", Request needs " + request.getRequestedStemCellType();
            }
        }

        return null;
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

        donorHLAProfileRepository.findTopByDonor_DonorIdOrderByEventTimestampDesc(donor.getDonorId()).ifPresent(hlaProfile -> dto.setHlaProfile(convertHLAProfileToDTO(hlaProfile)));

        List<LocationDTO> locations = donorLocationRepository.findLatestLocationsByDonorId(donor.getDonorId()).stream().map(this::convertLocationToDTO).collect(Collectors.toList());
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

        recipientHLAProfileRepository.findTopByRecipient_RecipientIdOrderByEventTimestampDesc(recipient.getRecipientId()).ifPresent(hlaProfile -> dto.setHlaProfile(convertHLAProfileToDTO(hlaProfile)));

        List<LocationDTO> locations = recipientLocationRepository.findLatestLocationsByRecipientId(recipient.getRecipientId()).stream().map(this::convertLocationToDTO).collect(Collectors.toList());
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

}
