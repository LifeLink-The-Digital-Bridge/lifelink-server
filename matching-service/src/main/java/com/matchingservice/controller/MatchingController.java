package com.matchingservice.controller;

import com.matchingservice.aop.RequireRole;
import com.matchingservice.dto.*;
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

    @RequireRole("DONOR")
    @GetMapping("/my-matches/as-donor")
    public ResponseEntity<List<MatchResponse>> getMyMatchesAsDonor(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(matchingService.getMatchesForDonor(userId));
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/my-matches/as-recipient")
    public ResponseEntity<List<MatchResponse>> getMyMatchesAsRecipient(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(matchingService.getMatchesForRecipient(userId));
    }

    @GetMapping("/admin/all-matches")
    public ResponseEntity<List<MatchResponse>> getAllMatches() {
        return ResponseEntity.ok(matchingService.getAllMatches());
    }

    @GetMapping("/donation/{donationId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatchesByDonation(@PathVariable UUID donationId) {
        return ResponseEntity.ok(matchingService.getMatchesByDonation(donationId));
    }

    @GetMapping("/request/{receiveRequestId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatchesByRequest(@PathVariable UUID receiveRequestId) {
        return ResponseEntity.ok(matchingService.getMatchesByRequest(receiveRequestId));
    }

    @RequireRole("DONOR")
    @PostMapping("/donor/confirm/{matchId}")
    public ResponseEntity<String> donorConfirmMatch(@PathVariable UUID matchId, @RequestHeader("id") UUID userId) {
        try {
            String result = matchingService.donorConfirmMatch(matchId, userId);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequireRole("RECIPIENT")
    @PostMapping("/recipient/confirm/{matchId}")
    public ResponseEntity<String> recipientConfirmMatch(@PathVariable UUID matchId, @RequestHeader("id") UUID userId) {
        try {
            String result = matchingService.recipientConfirmMatch(matchId, userId);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequireRole("DONOR")
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ReceiveRequestDTO> getRequestDetails(@PathVariable UUID requestId, @RequestHeader("id") UUID userId) {
        if (!matchingService.hasAccessToRequest(requestId, userId)) {
            System.out.println("In getRequestDetails Denied " + userId);
            return ResponseEntity.status(403).build();
        }
        System.out.println("In getRequestDetails");

        ReceiveRequestDTO requestDTO = matchingService.getRequestById(requestId);
        System.out.println(requestDTO);
        return ResponseEntity.ok(requestDTO);
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/donations/{donationId}")
    public ResponseEntity<DonationDTO> getDonationDetails(@PathVariable UUID donationId, @RequestHeader("id") UUID userId) {
        if (!matchingService.hasAccessToDonation(donationId, userId)) {
            System.out.println("In getDonationDetails Denied " + userId);
            return ResponseEntity.status(403).build();
        }
        System.out.println("In getDonationDetails");
        DonationDTO donationDTO = matchingService.getDonationById(donationId);
        System.out.println(donationDTO);
        return ResponseEntity.ok(donationDTO);
    }

}
