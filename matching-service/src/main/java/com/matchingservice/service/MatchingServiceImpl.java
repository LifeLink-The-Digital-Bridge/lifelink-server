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
        return matchResultRepository.findByDonorUserIdOrderByMatchedAtDesc(userId)
                .stream()
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesForRecipient(UUID userId) {
        return matchResultRepository.findByRecipientUserIdOrderByMatchedAtDesc(userId)
                .stream()
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getAllMatches() {
        return matchResultRepository.findAll()
                .stream()
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesByDonation(UUID donationId) {
        return matchResultRepository.findByDonationIdOrderByCompatibilityScoreDesc(donationId)
                .stream()
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getMatchesByRequest(UUID requestId) {
        return matchResultRepository.findByReceiveRequestIdOrderByCompatibilityScoreDesc(requestId)
                .stream()
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
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
                .map(MatchResponse::fromMatchResult)
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

            MatchResult matchResult = createMatchResult(donation, receiveRequest);
            MatchResult savedMatchResult = matchResultRepository.save(matchResult);

            return buildSuccessResponse(savedMatchResult);

        } catch (Exception e) {
            return buildErrorResponse("ERROR", e.getMessage());
        }
    }

    private String validateCompatibility(Donation donation, ReceiveRequest request) {
        if (!donation.getDonationType().toString().equals(request.getRequestType().toString())) {
            return "Type mismatch";
        }
        return null;
    }

    private MatchResult createMatchResult(Donation donation, ReceiveRequest request) {
        MatchResult match = new MatchResult();
        match.setDonationId(donation.getDonationId());
        match.setReceiveRequestId(request.getReceiveRequestId());
        match.setDonorUserId(donation.getUserId());
        match.setRecipientUserId(request.getRecipient().getUserId());
        match.setStatus(MatchStatus.PENDING);
        match.setMatchedAt(LocalDateTime.now());
        match.setIsConfirmed(false);
        return match;
    }

    private ManualMatchResponse buildSuccessResponse(MatchResult match) {
        return ManualMatchResponse.builder()
                .success(true)
                .matchResultId(match.getId())
                .message("Match created successfully")
                .build();
    }

    private ManualMatchResponse buildErrorResponse(String errorType, String message) {
        return ManualMatchResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    private DonorDTO convertDonorToDTO(Donor donor) {
        DonorDTO dto = new DonorDTO();
        dto.setDonorId(donor.getDonorId());
        dto.setUserId(donor.getUserId());
        dto.setRegistrationDate(donor.getRegistrationDate());
        dto.setStatus(donor.getStatus());
        return dto;
    }

    private RecipientDTO convertRecipientToDTO(Recipient recipient) {
        RecipientDTO dto = new RecipientDTO();
        dto.setRecipientId(recipient.getRecipientId());
        dto.setUserId(recipient.getUserId());
        dto.setAvailability(recipient.getAvailability());
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
        return dto;
    }

    private ReceiveRequestDTO convertToDTO(ReceiveRequest request) {
        ReceiveRequestDTO dto = new ReceiveRequestDTO();
        dto.setId(request.getReceiveRequestId());
        dto.setRecipientId(request.getRecipientId());
        dto.setRequestType(request.getRequestType());
        dto.setRequestedBloodType(request.getRequestedBloodType());
        dto.setRequestedOrgan(request.getRequestedOrgan());
        dto.setUrgencyLevel(request.getUrgencyLevel());
        dto.setQuantity(request.getQuantity());
        dto.setRequestDate(request.getRequestDate());
        dto.setStatus(request.getStatus());
        return dto;
    }
}
