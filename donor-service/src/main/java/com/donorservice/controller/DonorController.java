package com.donorservice.controller;

import com.donorservice.aop.InternalOnly;
import com.donorservice.aop.RequireRole;
import com.donorservice.client.UserClient;
import com.donorservice.dto.*;
import com.donorservice.dto.CreateDonationHistoryRequest;
import com.donorservice.enums.DonationStatus;
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

    public DonorController(UserClient userClient, DonorService donorService) {
        this.userClient = userClient;
        this.donorService = donorService;
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


    @RequireRole("DONOR")
    @GetMapping("/{id}")
    public DonorDTO getDonor(@PathVariable UUID id) {
        return donorService.getDonorById(id);
    }

    @RequireRole("DONOR")
    @GetMapping("/{donorId}/donations")
    public ResponseEntity<List<DonationDTO>> getDonations(@PathVariable UUID donorId) {
        return ResponseEntity.ok(donorService.getDonationsByDonorId(donorId));
    }

    @RequireRole("DONOR")
    @GetMapping("/my-donations")
    public ResponseEntity<List<DonationDTO>> getMyDonations(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(donorService.getDonationsByUserId(userId));
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

    @GetMapping("/donations/{donationId}")
    public ResponseEntity<DonationDTO> getDonationDetails(@PathVariable UUID donationId) {
        DonationDTO donation = donorService.getDonationById(donationId);
        return ResponseEntity.ok(donation);
    }

    @InternalOnly
    @PostMapping("/history/create")
    public ResponseEntity<String> createDonationHistory(@RequestBody CreateDonationHistoryRequest request) {
        donorService.createDonationHistory(request);
        return ResponseEntity.ok("Donation history created");
    }
    
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<DonorHistoryDTO>> getDonorHistory(@PathVariable UUID userId) {
        List<DonorHistoryDTO> history = donorService.getDonorHistory(userId);
        return ResponseEntity.ok(history);
    }
}
