package com.matchingservice.controller;

import com.matchingservice.aop.RequireRole;
import com.matchingservice.dto.*;
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

    @RequireRole("DONOR")
    @GetMapping("/donor/{donorUserId}/matches")
    public ResponseEntity<List<MatchedProfileResponse>> getMatchesForDonor(@PathVariable UUID donorUserId) {
        List<MatchedProfileResponse> matches = matchResultRepository.findByDonationDonorUserId(donorUserId)
                .stream()
                .map(MatchedProfileResponse::fromMatchResult)
                .collect(Collectors.toList());
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/donation/{donationId}/matches")
    public ResponseEntity<List<MatchedProfileResponse>> getMatchesByDonation(@PathVariable UUID donationId) {
        List<MatchedProfileResponse> matches = matchResultRepository.findByDonationDonationId(donationId)
                .stream()
                .map(MatchedProfileResponse::fromMatchResult)
                .collect(Collectors.toList());
        return ResponseEntity.ok(matches);
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/recipient/{recipientUserId}/matches")
    public ResponseEntity<List<MatchedProfileResponse>> getMatchesForRecipient(@PathVariable UUID recipientUserId) {
        List<MatchedProfileResponse> matches = matchResultRepository.findByReceiveRequestRecipientId(recipientUserId)
                .stream()
                .map(MatchedProfileResponse::fromMatchResult)
                .collect(Collectors.toList());
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/request/{receiveRequestId}/matches")
    public ResponseEntity<List<MatchedProfileResponse>> getMatchesByRequest(@PathVariable UUID receiveRequestId) {
        List<MatchedProfileResponse> matches = matchResultRepository.findByReceiveRequestReceiveRequestId(receiveRequestId)
                .stream()
                .map(MatchedProfileResponse::fromMatchResult)
                .collect(Collectors.toList());
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/match-details/{matchId}")
    public ResponseEntity<MatchDetailsResponse> getMatchDetails(@PathVariable UUID matchId) {
        return matchResultRepository.findById(matchId)
                .map(match -> {
                    MatchDetailsResponse.MatchDetailsResponseBuilder builder = MatchDetailsResponse.builder()
                            .matchId(match.getId())
                            .isConfirmed(match.getIsConfirmed())
                            .matchedAt(match.getMatchedAt())
                            .distance(match.getDistance());

                    if (match.getDonation() != null && match.getDonation().getDonor() != null) {
                        builder.donorId(match.getDonation().getDonor().getUserId())
                                .donationId(match.getDonation().getDonationId());
                    }
                    if (match.getReceiveRequest() != null) {
                        builder.recipientId(match.getReceiveRequest().getRecipientId())
                                .receiveRequestId(match.getReceiveRequest().getReceiveRequestId());
                    }

                    return ResponseEntity.ok(builder.build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/all-matches")
    public ResponseEntity<List<MatchResultResponse>> getAllMatches() {
        List<MatchResultResponse> matches = matchResultRepository.findAll()
                .stream()
                .map(MatchResultResponse::fromMatchResult)
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
