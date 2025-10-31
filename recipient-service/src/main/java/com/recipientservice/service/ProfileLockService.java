package com.recipientservice.service;

import com.recipientservice.dto.ProfileLockInfoDTO;
import com.recipientservice.enums.RequestStatus;
import com.recipientservice.model.ReceiveRequest;
import com.recipientservice.repository.ReceiveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileLockService {

    private final ReceiveRequestRepository receiveRequestRepository;

    private static final List<RequestStatus> LOCKING_STATUSES = Arrays.asList(
            RequestStatus.PENDING,
            RequestStatus.MATCHED,
            RequestStatus.IN_PROGRESS
    );

    public boolean isRecipientProfileLocked(UUID recipientId) {
        return receiveRequestRepository.existsByRecipientIdAndStatusIn(recipientId, LOCKING_STATUSES);
    }

    public String getProfileLockReason(UUID recipientId) {
        if (!isRecipientProfileLocked(recipientId)) {
            return null;
        }

        List<ReceiveRequest> activeRequests = receiveRequestRepository
                .findByRecipientIdAndStatusIn(recipientId, LOCKING_STATUSES);

        StringBuilder reason = new StringBuilder("Your profile is locked due to:\n\n");

        for (ReceiveRequest request : activeRequests) {
            reason.append("• ")
                    .append(request.getRequestType())
                    .append(" request (Status: ")
                    .append(request.getStatus().getDescription())
                    .append(")\n");
        }

        reason.append("\n")
                .append("You can cancel pending requests to unlock your profile.\n")
                .append("Note: Requests in IN_PROGRESS status require admin approval for cancellation.");

        return reason.toString();
    }

    public ProfileLockInfoDTO getDetailedLockInfo(UUID recipientId) {
        boolean isLocked = isRecipientProfileLocked(recipientId);

        if (!isLocked) {
            return ProfileLockInfoDTO.builder()
                    .isLocked(false)
                    .reason("Profile is not locked")
                    .activeRequests(List.of())
                    .lockedFields(List.of())
                    .editableFields(getAllEditableFields())
                    .build();
        }

        List<ReceiveRequest> activeRequests = receiveRequestRepository
                .findByRecipientIdAndStatusIn(recipientId, LOCKING_STATUSES);

        List<ProfileLockInfoDTO.ActiveRequestInfo> requestInfos = activeRequests.stream()
                .map(this::convertToRequestInfo)
                .collect(Collectors.toList());

        return ProfileLockInfoDTO.builder()
                .isLocked(true)
                .reason(getProfileLockReason(recipientId))
                .activeRequests(requestInfos)
                .lockedFields(getLockedFields())
                .editableFields(getEditableFields())
                .build();
    }

    private ProfileLockInfoDTO.ActiveRequestInfo convertToRequestInfo(ReceiveRequest request) {
        boolean canCancel = request.getStatus() != RequestStatus.IN_PROGRESS;
        String restriction = canCancel ? null : "Requires admin approval";

        return ProfileLockInfoDTO.ActiveRequestInfo.builder()
                .requestId(request.getId())
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .requestDate(request.getRequestDate())
                .canBeCancelled(canCancel)
                .cancelRestrictionReason(restriction)
                .build();
    }

    public void releaseLock(UUID recipientId) {
        boolean hasActiveRequests = isRecipientProfileLocked(recipientId);

        if (!hasActiveRequests) {
            System.out.println("Profile lock released for recipient: " + recipientId);
        }
    }

    private List<String> getLockedFields() {
        return Arrays.asList(
                "requestedBloodType",
                "hlaProfile",
                "medicalDetails.hemoglobinLevel",
                "medicalDetails.bloodGlucoseLevel",
                "medicalDetails.hasDiabetes",
                "medicalDetails.bloodPressure",
                "medicalDetails.diagnosis",
                "medicalDetails.hasInfectiousDiseases",
                "eligibilityCriteria.age",
                "eligibilityCriteria.weight",
                "eligibilityCriteria.height",
                "eligibilityCriteria.dob",
                "eligibilityCriteria.medicallyEligible"
        );
    }

    private List<String> getEditableFields() {
        return Arrays.asList(
                "contactPreferences",
                "notificationSettings",
                "addresses",
                "consentForm.additionalNotes"
        );
    }

    private List<String> getAllEditableFields() {
        List<String> all = new ArrayList<>(getLockedFields());
        all.addAll(getEditableFields());
        return all;
    }
}
