package com.matchingservice.service;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;

public interface MatchingService {
    ManualMatchResponse manualMatch(ManualMatchRequest request);
}
