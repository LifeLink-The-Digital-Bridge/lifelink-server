package com.matchingservice.service;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;
import com.matchingservice.dto.MatchResponse;

import java.util.List;
import java.util.UUID;

public interface MatchingService {
    ManualMatchResponse manualMatch(ManualMatchRequest request);
    List<MatchResponse> getMatchesByDonation(UUID donationId);
    List<MatchResponse> getMatchesByRequest(UUID receiveRequestId);
    MatchResponse getMatchById(UUID matchId);
}
