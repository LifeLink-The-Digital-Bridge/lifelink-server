package com.donorservice.service;

import com.donorservice.dto.ProfileLockInfoDTO;
import com.donorservice.enums.DonationStatus;
import com.donorservice.model.Donation;
import com.donorservice.repository.DonationRepository;
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

    private final DonationRepository donationRepository;

    private static final List<DonationStatus> LOCKING_STATUSES = Arrays.asList(
            DonationStatus.PENDING,
            DonationStatus.MATCHED,
            DonationStatus.IN_PROGRESS
    );

    public boolean isDonorProfileLocked(UUID donorId) {
        return donationRepository.existsByDonorIdAndStatusIn(donorId, LOCKING_STATUSES);
    }

    public String getProfileLockReason(UUID donorId) {
        if (!isDonorProfileLocked(donorId)) {
            return null;
        }

        List<Donation> activeDonations = donationRepository
                .findByDonorIdAndStatusIn(donorId, LOCKING_STATUSES);

        StringBuilder reason = new StringBuilder("Your profile is locked due to:\n\n");

        for (Donation donation : activeDonations) {
            reason.append("• ")
                    .append(donation.getDonationType())
                    .append(" donation (Status: ")
                    .append(donation.getStatus().getDescription())
                    .append(")\n");
        }

        reason.append("\n")
                .append("You can cancel pending donations to unlock your profile.\n")
                .append("Note: Donations in IN_PROGRESS status require admin approval for cancellation.");

        return reason.toString();
    }

    public ProfileLockInfoDTO getDetailedLockInfo(UUID donorId) {
        boolean isLocked = isDonorProfileLocked(donorId);

        if (!isLocked) {
            return ProfileLockInfoDTO.builder()
                    .isLocked(false)
                    .reason("Profile is not locked")
                    .activeDonations(List.of())
                    .lockedFields(List.of())
                    .editableFields(getAllEditableFields())
                    .build();
        }

        List<Donation> activeDonations = donationRepository
                .findByDonorIdAndStatusIn(donorId, LOCKING_STATUSES);

        List<ProfileLockInfoDTO.ActiveDonationInfo> donationInfos = activeDonations.stream()
                .map(this::convertToDonationInfo)
                .collect(Collectors.toList());

        return ProfileLockInfoDTO.builder()
                .isLocked(true)
                .reason(getProfileLockReason(donorId))
                .activeDonations(donationInfos)
                .lockedFields(getLockedFields())
                .editableFields(getEditableFields())
                .build();
    }

    private ProfileLockInfoDTO.ActiveDonationInfo convertToDonationInfo(Donation donation) {
        boolean canCancel = donation.getStatus() != DonationStatus.IN_PROGRESS;
        String restriction = canCancel ? null : "Requires admin approval";

        return ProfileLockInfoDTO.ActiveDonationInfo.builder()
                .donationId(donation.getId())
                .donationType(donation.getDonationType())
                .status(donation.getStatus())
                .donationDate(donation.getDonationDate())
                .canBeCancelled(canCancel)
                .cancelRestrictionReason(restriction)
                .build();
    }

    public void releaseLock(UUID donorId) {
        boolean hasActiveDonations = isDonorProfileLocked(donorId);

        if (!hasActiveDonations) {
            System.out.println("Profile lock released for donor: " + donorId);
        }
    }

    private List<String> getLockedFields() {
        return Arrays.asList(
                "bloodType",
                "hlaProfile",
                "medicalDetails.hemoglobinLevel",
                "medicalDetails.bloodGlucoseLevel",
                "medicalDetails.hasDiabetes",
                "medicalDetails.bloodPressure",
                "medicalDetails.hasInfectiousDiseases",
                "eligibilityCriteria.age",
                "eligibilityCriteria.weight",
                "eligibilityCriteria.height",
                "eligibilityCriteria.dob",
                "eligibilityCriteria.medicalClearance"
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
