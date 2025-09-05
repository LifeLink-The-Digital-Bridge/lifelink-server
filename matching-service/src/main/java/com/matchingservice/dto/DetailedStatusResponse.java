package com.matchingservice.dto;

import com.matchingservice.model.donor.Donation;
import com.matchingservice.model.recipients.ReceiveRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedStatusResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String status;
    private String type;
    private String bloodType;
    private String donationType;
    private String organType;
    private String tissueType;
    private String stemCellType;
    private String requestType;
    private String urgencyLevel;
    private Double quantity;
    private LocalDate requestDate;
    private LocalDate donationDate;
    private String notes;
    private String city;
    private String state;

    public static DetailedStatusResponse fromDonation(Donation donation) {
        UUID userId = null;
        String userName = null;
        String userEmail = null;

        if (donation.getDonor() != null) {
            userId = donation.getDonor().getId();
        }

        return DetailedStatusResponse.builder()
                .id(donation.getDonationId())
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .status(donation.getStatus() != null ? donation.getStatus().toString() : null)
                .type("DONATION")
                .bloodType(donation.getBloodType() != null ? donation.getBloodType().toString() : null)
                .donationType(donation.getDonationType() != null ? donation.getDonationType().toString() : null)
                .donationDate(donation.getDonationDate())
                .city(donation.getLocationSummary() != null ? donation.getLocationSummary().getCity() : null)
                .state(donation.getLocationSummary() != null ? donation.getLocationSummary().getState() : null)
                .build();
    }

    public static DetailedStatusResponse fromReceiveRequest(ReceiveRequest request) {
        return DetailedStatusResponse.builder()
                .id(request.getReceiveRequestId())
                .userId(request.getRecipientId())
                .status(request.getStatus() != null ? request.getStatus().toString() : null)
                .type("RECEIVE_REQUEST")
                .bloodType(request.getRequestedBloodType() != null ? request.getRequestedBloodType().toString() : null)
                .organType(request.getRequestedOrgan() != null ? request.getRequestedOrgan().toString() : null)
                .tissueType(request.getRequestedTissue() != null ? request.getRequestedTissue().toString() : null)
                .stemCellType(request.getRequestedStemCellType() != null ? request.getRequestedStemCellType().toString() : null)
                .requestType(request.getRequestType() != null ? request.getRequestType().toString() : null)
                .urgencyLevel(request.getUrgencyLevel() != null ? request.getUrgencyLevel().toString() : null)
                .quantity(request.getQuantity())
                .requestDate(request.getRequestDate())
                .notes(request.getNotes())
                .city(request.getLocation() != null ? request.getLocation().getCity() : null)
                .state(request.getLocation() != null ? request.getLocation().getState() : null)
                .build();
    }
}
