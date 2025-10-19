package com.matchingservice.service;

import com.matchingservice.dto.*;

import java.util.List;
import java.util.UUID;

public interface MatchingService {
    ManualMatchResponse manualMatch(ManualMatchRequest request);

    String donorConfirmMatch(UUID matchId, UUID userId);

    String recipientConfirmMatch(UUID matchId, UUID userId);

    List<MatchResponse> getMatchesForDonor(UUID donorUserId);

    List<MatchResponse> getMatchesForRecipient(UUID recipientUserId);

    List<MatchResponse> getAllMatches();

    List<MatchResponse> getMatchesByDonation(UUID donationId);

    List<MatchResponse> getMatchesByRequest(UUID receiveRequestId);

    boolean hasAccessToDonation(UUID donationId, UUID userId);

    boolean hasAccessToRequest(UUID requestId, UUID userId);

    ReceiveRequestDTO getRequestById(UUID requestId);

    DonationDTO getDonationById(UUID donationId);

    boolean isMatchConfirmed(UUID matchId);

    DonorDTO getDonorByUserId(UUID userId);

    RecipientDTO getRecipientByUserId(UUID userId);

    List<MatchResponse> getPendingMatchesForUser(UUID userId);

    List<MatchResponse> getActiveMatchesForUser(UUID userId);

    List<MatchResponse> getConfirmedMatchesForUser(UUID userId);

    DonorDTO getDonorSnapshotByDonation(UUID donationId);

    RecipientDTO getRecipientSnapshotByRequest(UUID requestId);

    boolean hasAccessToDonorSnapshot(UUID donationId, UUID userId);

    boolean hasAccessToRecipientSnapshot(UUID requestId, UUID userId);

    String donorRejectMatch(UUID matchId, UUID userId, String rejectionReason);

    String recipientRejectMatch(UUID matchId, UUID userId, String rejectionReason);

    String donorWithdrawConfirmation(UUID matchId, UUID userId, String withdrawalReason);

    String recipientWithdrawConfirmation(UUID matchId, UUID userId, String withdrawalReason);

    String recipientConfirmCompletion(UUID matchId, UUID userId, CompletionConfirmationDTO details);
}
