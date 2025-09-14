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
}
