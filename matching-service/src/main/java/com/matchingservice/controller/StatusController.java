package com.matchingservice.controller;

import com.matchingservice.aop.RequireRole;
import com.matchingservice.dto.StatusResponse;
import com.matchingservice.dto.DetailedStatusResponse;
import com.matchingservice.enums.*;
import com.matchingservice.exceptions.InvalidStatusException;
import com.matchingservice.repository.DonationRepository;
import com.matchingservice.repository.ReceiveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matching/status")
@RequiredArgsConstructor
public class StatusController {

    private final DonationRepository donationRepository;
    private final ReceiveRequestRepository receiveRequestRepository;

    @GetMapping("/donation/status/{donationId}")
    public ResponseEntity<StatusResponse> getDonationStatus(@PathVariable UUID donationId) {
        return donationRepository.findById(donationId)
                .map(donation -> ResponseEntity.ok(new StatusResponse(donation.getStatus().toString())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/request/status/{requestId}")
    public ResponseEntity<StatusResponse> getRequestStatus(@PathVariable UUID requestId) {
        return receiveRequestRepository.findById(requestId)
                .map(request -> ResponseEntity.ok(new StatusResponse(request.getStatus().toString())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/donation/details/{donationId}")
    public ResponseEntity<DetailedStatusResponse> getDonationDetails(@PathVariable UUID donationId) {
        return donationRepository.findById(donationId)
                .map(donation -> ResponseEntity.ok(DetailedStatusResponse.fromDonation(donation)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/request/details/{requestId}")
    public ResponseEntity<DetailedStatusResponse> getRequestDetails(@PathVariable UUID requestId) {
        return receiveRequestRepository.findById(requestId)
                .map(request -> ResponseEntity.ok(DetailedStatusResponse.fromReceiveRequest(request)))
                .orElse(ResponseEntity.notFound().build());
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/donations")
    public ResponseEntity<List<DetailedStatusResponse>> getAllDonations() {
        List<DetailedStatusResponse> donations = donationRepository.findAll()
                .stream()
                .map(DetailedStatusResponse::fromDonation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donations);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests")
    public ResponseEntity<List<DetailedStatusResponse>> getAllRequests() {
        List<DetailedStatusResponse> requests = receiveRequestRepository.findAll()
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/donations/status/{status}")
    public ResponseEntity<List<DetailedStatusResponse>> getDonationsByStatus(@PathVariable String status) {
        DonationStatus donationStatus;
        try {
            donationStatus = DonationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid donation status: " + status);
        }
        List<DetailedStatusResponse> donations = donationRepository.findByStatus(donationStatus)
                .stream()
                .map(DetailedStatusResponse::fromDonation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donations);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests/status/{status}")
    public ResponseEntity<List<DetailedStatusResponse>> getRequestsByStatus(@PathVariable String status) {
        RequestStatus requestStatus;
        try {
            requestStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid request status: " + status);
        }
        List<DetailedStatusResponse> requests = receiveRequestRepository.findByStatus(requestStatus)
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/donations/blood-type/{bloodType}")
    public ResponseEntity<List<DetailedStatusResponse>> getDonationsByBloodType(@PathVariable String bloodType) {
        BloodType type;
        try {
            type = BloodType.valueOf(bloodType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid blood type: " + bloodType);
        }
        List<DetailedStatusResponse> donations = donationRepository.findByBloodType(type)
                .stream()
                .map(DetailedStatusResponse::fromDonation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donations);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests/blood-type/{bloodType}")
    public ResponseEntity<List<DetailedStatusResponse>> getRequestsByBloodType(@PathVariable String bloodType) {
        BloodType type;
        try {
            type = BloodType.valueOf(bloodType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid blood type: " + bloodType);
        }
        List<DetailedStatusResponse> requests = receiveRequestRepository.findByRequestedBloodType(type)
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests/urgency/{urgencyLevel}")
    public ResponseEntity<List<DetailedStatusResponse>> getRequestsByUrgency(@PathVariable String urgencyLevel) {
        UrgencyLevel level;
        try {
            level = UrgencyLevel.valueOf(urgencyLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid urgency level: " + urgencyLevel);
        }
        List<DetailedStatusResponse> requests = receiveRequestRepository.findByUrgencyLevel(level)
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/donations/recent")
    public ResponseEntity<List<DetailedStatusResponse>> getRecentDonations() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<DetailedStatusResponse> donations = donationRepository.findRecentDonations(thirtyDaysAgo)
                .stream()
                .map(DetailedStatusResponse::fromDonation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donations);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests/recent")
    public ResponseEntity<List<DetailedStatusResponse>> getRecentRequests() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<DetailedStatusResponse> requests = receiveRequestRepository.findRecentRequests(thirtyDaysAgo)
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests/urgent")
    public ResponseEntity<List<DetailedStatusResponse>> getUrgentRequests() {
        List<DetailedStatusResponse> requests = receiveRequestRepository.findUrgentRequests()
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/donations/count/{status}")
    public ResponseEntity<Long> getDonationsCountByStatus(@PathVariable String status) {
        DonationStatus donationStatus;
        try {
            donationStatus = DonationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid donation status: " + status);
        }
        long count = donationRepository.countByStatus(donationStatus);
        return ResponseEntity.ok(count);
    }

    @RequireRole("ADMIN")
    @GetMapping("/admin/requests/count/{status}")
    public ResponseEntity<Long> getRequestsCountByStatus(@PathVariable String status) {
        RequestStatus requestStatus;
        try {
            requestStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid request status: " + status);
        }
        long count = receiveRequestRepository.countByStatus(requestStatus);
        return ResponseEntity.ok(count);
    }
}
