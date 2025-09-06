package com.matchingservice.controller;

import com.matchingservice.aop.RequireRole;
import com.matchingservice.dto.StatusResponse;
import com.matchingservice.dto.DetailedStatusResponse;
import com.matchingservice.enums.*;
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

    @GetMapping("/recipient/status/{requestId}")
    public ResponseEntity<StatusResponse> getRecipientStatus(@PathVariable UUID requestId) {
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

    @GetMapping("/recipient/details/{requestId}")
    public ResponseEntity<DetailedStatusResponse> getRecipientDetails(@PathVariable UUID requestId) {
        return receiveRequestRepository.findById(requestId)
                .map(request -> ResponseEntity.ok(DetailedStatusResponse.fromReceiveRequest(request)))
                .orElse(ResponseEntity.notFound().build());
    }

    @RequireRole("ADMIN")
    @GetMapping("/donations")
    public ResponseEntity<List<DetailedStatusResponse>> getAllDonations() {
        List<DetailedStatusResponse> donations = donationRepository.findAll()
                .stream()
                .map(DetailedStatusResponse::fromDonation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donations);
    }

    @RequireRole("ADMIN")
    @GetMapping("/requests")
    public ResponseEntity<List<DetailedStatusResponse>> getAllReceiveRequests() {
        List<DetailedStatusResponse> requests = receiveRequestRepository.findAll()
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @RequireRole("ADMIN")
    @GetMapping("/donations/status/{status}")
    public ResponseEntity<List<DetailedStatusResponse>> getDonationsByStatus(@PathVariable String status) {
        try {
            DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
            List<DetailedStatusResponse> donations = donationRepository.findByStatusOrderByDonationDateDesc(donationStatus)
                    .stream()
                    .map(DetailedStatusResponse::fromDonation)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(donations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequireRole("ADMIN")
    @GetMapping("/requests/status/{status}")
    public ResponseEntity<List<DetailedStatusResponse>> getRequestsByStatus(@PathVariable String status) {
        try {
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            List<DetailedStatusResponse> requests = receiveRequestRepository.findByStatusOrderByRequestDateDesc(requestStatus)
                    .stream()
                    .map(DetailedStatusResponse::fromReceiveRequest)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/donations/blood-type/{bloodType}")
    public ResponseEntity<List<DetailedStatusResponse>> getDonationsByBloodType(@PathVariable String bloodType) {
        try {
            BloodType type = BloodType.valueOf(bloodType.toUpperCase());
            List<DetailedStatusResponse> donations = donationRepository.findByBloodType(type)
                    .stream()
                    .map(DetailedStatusResponse::fromDonation)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(donations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/requests/blood-type/{bloodType}")
    public ResponseEntity<List<DetailedStatusResponse>> getRequestsByBloodType(@PathVariable String bloodType) {
        try {
            BloodType type = BloodType.valueOf(bloodType.toUpperCase());
            List<DetailedStatusResponse> requests = receiveRequestRepository.findByRequestedBloodType(type)
                    .stream()
                    .map(DetailedStatusResponse::fromReceiveRequest)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/requests/urgency/{urgencyLevel}")
    public ResponseEntity<List<DetailedStatusResponse>> getRequestsByUrgency(@PathVariable String urgencyLevel) {
        try {
            UrgencyLevel level = UrgencyLevel.valueOf(urgencyLevel.toUpperCase());
            List<DetailedStatusResponse> requests = receiveRequestRepository.findByUrgencyLevel(level)
                    .stream()
                    .map(DetailedStatusResponse::fromReceiveRequest)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/donations/recent")
    public ResponseEntity<List<DetailedStatusResponse>> getRecentDonations() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<DetailedStatusResponse> donations = donationRepository.findRecentDonations(thirtyDaysAgo)
                .stream()
                .map(DetailedStatusResponse::fromDonation)
                .collect(Collectors.toList());
        return ResponseEntity.ok(donations);
    }

    @GetMapping("/requests/recent")
    public ResponseEntity<List<DetailedStatusResponse>> getRecentRequests() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<DetailedStatusResponse> requests = receiveRequestRepository.findRecentRequests(thirtyDaysAgo)
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/requests/urgent")
    public ResponseEntity<List<DetailedStatusResponse>> getUrgentRequests() {
        List<DetailedStatusResponse> requests = receiveRequestRepository.findUrgentRequests()
                .stream()
                .map(DetailedStatusResponse::fromReceiveRequest)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/donations/count/{status}")
    public ResponseEntity<Long> getDonationsCountByStatus(@PathVariable String status) {
        try {
            DonationStatus donationStatus = DonationStatus.valueOf(status.toUpperCase());
            long count = donationRepository.countByStatus(donationStatus);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/requests/count/{status}")
    public ResponseEntity<Long> getRequestsCountByStatus(@PathVariable String status) {
        try {
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            long count = receiveRequestRepository.countByStatus(requestStatus);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
