package com.donorservice.controller;

import com.donorservice.aop.InternalOnly;
import com.donorservice.aop.RequireRole;
import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.enums.DonationStatus;
import com.donorservice.exception.AccessDeniedException;
import com.donorservice.model.Donor;
import com.donorservice.repository.DonorRepository;
import com.donorservice.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final UserClient userClient;
    private final DonorService donorService;
    private final DonorRepository donorRepository;

    public DonorController(UserClient userClient, DonorService donorService, DonorRepository donorRepository) {
        this.userClient = userClient;
        this.donorService = donorService;
        this.donorRepository = donorRepository;
    }

    @PutMapping("/addRole")
    public ResponseEntity<String> addRoleToUser(@RequestHeader("id") String userId) {
        userClient.addRole(UUID.fromString(userId), "DONOR");
        return ResponseEntity.ok("Role added");
    }

    @RequireRole("DONOR")
    @PostMapping("/profile")
    public ResponseEntity<?> saveOrUpdateDonor(@RequestHeader("id") UUID userId, @RequestBody RegisterDonor donorDTO) {
        try {
            DonorDTO result = donorService.saveOrUpdateDonor(userId, donorDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @RequireRole("DONOR")
    @PostMapping("/donate")
    public ResponseEntity<DonationDTO> registerDonation(@RequestBody DonationRequestDTO donationDTO) {
        DonationDTO response = donorService.registerDonation(donationDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-userId")
    public ResponseEntity<DonorDTO> getDonorByUserId(@RequestHeader("id") UUID userId) {
        DonorDTO donor = donorService.getDonorByUserId(userId);
        if (donor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donor);
    }

    @GetMapping("/by-userId/{userId}")
    public ResponseEntity<DonorDTO> getDonorByUser(@PathVariable UUID userId) {
        DonorDTO donor = donorService.getDonorByUserId(userId);
        if (donor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donor);
    }

    @RequireRole("DONOR")
    @GetMapping("/{id}")
    public DonorDTO getDonor(@PathVariable UUID id) {
        return donorService.getDonorById(id);
    }

    @GetMapping("/{donorId}/donations")
    public ResponseEntity<?> getDonations(@PathVariable UUID donorId, @RequestHeader("id") String requesterId) {

        try {
            UUID requesterUUID = UUID.fromString(requesterId);
            return ResponseEntity.ok(donorService.getDonationsByDonorId(donorId, requesterUUID));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @RequireRole("DONOR")
    @GetMapping("/my-donations")
    public ResponseEntity<List<DonationDTO>> getMyDonations(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(donorService.getDonationsByUserId(userId));
    }

    @GetMapping("/by-userId/{userId}/donations")
    public ResponseEntity<?> getDonationsByUserId(@PathVariable UUID userId, @RequestHeader("id") String requesterId) {

        try {
            UUID requesterUUID = UUID.fromString(requesterId);
            Donor donor = donorRepository.findByUserId(userId);

            if (donor == null) {
                return ResponseEntity.ok(List.of());
            }
            List<DonationDTO> donations = donorService.getDonationsByDonorId(donor.getId(), requesterUUID);
            return ResponseEntity.ok(donations);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }


    @InternalOnly
    @PutMapping("/donations/{donationId}/status")
    public ResponseEntity<String> updateDonationStatus(@PathVariable UUID donationId, @RequestBody DonationStatus status) {
        donorService.updateDonationStatus(donationId, status);
        return ResponseEntity.ok("Donation status updated to " + status);
    }


    @RequireRole("DONOR")
    @GetMapping("/donations/{donationId}/status")
    public ResponseEntity<String> getDonationStatus(@PathVariable UUID donationId) {
        String status = donorService.getDonationStatus(donationId);
        return ResponseEntity.ok(status);
    }


    @RequireRole("DONOR")
    @PostMapping("/donations/{donationId}/cancel")
    public ResponseEntity<?> cancelDonation(@PathVariable UUID donationId, @RequestHeader("id") UUID userId, @Valid @RequestBody CancellationRequestDTO request) {
        try {
            CancellationResponseDTO response = donorService.cancelDonation(donationId, userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @RequireRole("DONOR")
    @GetMapping("/donations/{donationId}/can-cancel")
    public ResponseEntity<Map<String, Boolean>> canCancelDonation(@PathVariable UUID donationId, @RequestHeader("id") UUID userId) {
        boolean canCancel = donorService.canCancelDonation(donationId, userId);
        return ResponseEntity.ok(Map.of("canCancel", canCancel));
    }

    @RequireRole("DONOR")
    @GetMapping("/profile-lock-info")
    public ResponseEntity<ProfileLockInfoDTO> getProfileLockInfo(@RequestHeader("id") UUID userId) {
        ProfileLockInfoDTO lockInfo = donorService.getProfileLockInfo(userId);
        return ResponseEntity.ok(lockInfo);
    }

}
