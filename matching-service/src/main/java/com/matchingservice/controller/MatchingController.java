package com.matchingservice.controller;

import com.matchingservice.dto.ManualMatchRequest;
import com.matchingservice.dto.ManualMatchResponse;
import com.matchingservice.dto.MatchResponse;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.repository.MatchResultRepository;
import com.matchingservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final MatchResultRepository matchResultRepository;

    @PostMapping("/manual-match")
    public ResponseEntity<ManualMatchResponse> manualMatch(@RequestBody ManualMatchRequest request) {
        ManualMatchResponse response = matchingService.manualMatch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/donation/{donationId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatchesByDonation(@PathVariable UUID donationId) {
        return ResponseEntity.ok(matchingService.getMatchesByDonation(donationId));
    }

    @GetMapping("/request/{receiveRequestId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatchesByRequest(@PathVariable UUID receiveRequestId) {
        return ResponseEntity.ok(matchingService.getMatchesByRequest(receiveRequestId));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable UUID matchId) {
        try {
            return ResponseEntity.ok(matchingService.getMatchById(matchId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/all-matches")
    public ResponseEntity<List<MatchResponse>> getAllMatches() {
        List<MatchResponse> matches = matchResultRepository.findAll()
                .stream()
                .map(MatchResponse::fromMatchResult)
                .collect(Collectors.toList());
        return ResponseEntity.ok(matches);
    }

    @PutMapping("/match/{matchId}/confirm")
    public ResponseEntity<String> confirmMatch(@PathVariable UUID matchId) {
        return matchResultRepository.findById(matchId)
                .map(match -> {
                    match.setIsConfirmed(true);
                    matchResultRepository.save(match);
                    return ResponseEntity.ok("Match confirmed successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
