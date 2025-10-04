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
    public ResponseEntity<?> getDonations(
            @PathVariable UUID donorId,
            @RequestHeader("id") String requesterId) {

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
    public ResponseEntity<?> getDonationsByUserId(
            @PathVariable UUID userId,
            @RequestHeader("id") String requesterId) {

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
    @PutMapping("/donations/{donationId}/status/completed")
    public ResponseEntity<String> updateDonationStatusToCompleted(@PathVariable UUID donationId) {
        donorService.updateDonationStatus(donationId, DonationStatus.COMPLETED);
        return ResponseEntity.ok("Donation status updated to completed");
    }

    @RequireRole("DONOR")
    @GetMapping("/donations/{donationId}/status")
    public ResponseEntity<String> getDonationStatus(@PathVariable UUID donationId) {
        String status = donorService.getDonationStatus(donationId);
        return ResponseEntity.ok(status);
    }

}
