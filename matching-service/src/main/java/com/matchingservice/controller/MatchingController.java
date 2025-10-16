package com.matchingservice.controller;

import com.matchingservice.aop.RequireRole;
import com.matchingservice.dto.*;
import com.matchingservice.exceptions.ResourceNotFoundException;
import com.matchingservice.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

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

    @RequireRole("RECIPIENT")
    @GetMapping("/donor-details/{userId}")
    public ResponseEntity<DonorDTO> getDonorDetails(@PathVariable UUID userId){
        try {
            DonorDTO donorDTO = matchingService.getDonorByUserId(userId);
            return ResponseEntity.ok(donorDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequireRole("DONOR")
    @GetMapping("/recipient-details/{userId}")
    public ResponseEntity<RecipientDTO> getRecipientDetails(@PathVariable UUID userId){
        try {
            RecipientDTO recipientDTO = matchingService.getRecipientByUserId(userId);
            return ResponseEntity.ok(recipientDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/donations/{donationId}")
    public ResponseEntity<DonationDTO> getDonationDetails(@PathVariable UUID donationId, @RequestHeader("id") UUID userId) {
        if (!matchingService.hasAccessToDonation(donationId, userId)) {
            return ResponseEntity.status(403).build();
        }
        try {
            DonationDTO donationDTO = matchingService.getDonationById(donationId);
            return ResponseEntity.ok(donationDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ReceiveRequestDTO> getRequestDetails(@PathVariable UUID requestId, @RequestHeader("id") UUID userId) {
        if (!matchingService.hasAccessToRequest(requestId, userId)) {
            return ResponseEntity.status(403).build();
        }
        try {
            ReceiveRequestDTO requestDTO = matchingService.getRequestById(requestId);
            return ResponseEntity.ok(requestDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/donations/{donationId}/donor-snapshot")
    public ResponseEntity<DonorDTO> getDonorSnapshotByDonation(@PathVariable UUID donationId, @RequestHeader("id") UUID userId) {
        if (!matchingService.hasAccessToDonorSnapshot(donationId, userId)) {
            System.out.println("Access Denied to Donor Snapshot for donationId: " + donationId + ", userId: " + userId);
            return ResponseEntity.status(403).build();
        }
        try {
            DonorDTO donorSnapshot = matchingService.getDonorSnapshotByDonation(donationId);
            return ResponseEntity.ok(donorSnapshot);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/requests/{requestId}/recipient-snapshot")
    public ResponseEntity<RecipientDTO> getRecipientSnapshotByRequest(@PathVariable UUID requestId, @RequestHeader("id") UUID userId) {
        if (!matchingService.hasAccessToRecipientSnapshot(requestId, userId)) {
            System.out.println("Access Denied to Recipient Snapshot for requestId: " + requestId + ", userId: " + userId);
            return ResponseEntity.status(403).build();
        }
        try {
            RecipientDTO recipientSnapshot = matchingService.getRecipientSnapshotByRequest(requestId);
            return ResponseEntity.ok(recipientSnapshot);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-matches/active")
    public ResponseEntity<List<MatchResponse>> getActiveMatches(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(matchingService.getActiveMatchesForUser(userId));
    }

    @GetMapping("/my-matches/pending")
    public ResponseEntity<List<MatchResponse>> getPendingMatches(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(matchingService.getPendingMatchesForUser(userId));
    }

    @GetMapping("/my-matches/confirmed")
    public ResponseEntity<List<MatchResponse>> getConfirmedMatches(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(matchingService.getConfirmedMatchesForUser(userId));
    }

    @GetMapping("/match/{matchId}/status")
    public ResponseEntity<Boolean> getMatchConfirmationStatus(@PathVariable UUID matchId) {
        boolean isConfirmed = matchingService.isMatchConfirmed(matchId);
        return ResponseEntity.ok(isConfirmed);
    }
}
