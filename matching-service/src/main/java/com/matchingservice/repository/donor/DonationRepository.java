package com.matchingservice.repository.donor;

import com.matchingservice.enums.BloodType;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.DonationType;
import com.matchingservice.model.donor.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findByDonor_DonorIdOrderByEventTimestampDesc(UUID donorId);

    @Query("""
        SELECT d FROM Donation d 
        WHERE d.donationType = :donationType 
        AND d.bloodType = :bloodType 
        AND d.status = :status
        ORDER BY d.eventTimestamp DESC
    """)
    List<Donation> findCompatibleDonations(
            @Param("donationType") DonationType donationType,
            @Param("bloodType") BloodType bloodType,
            @Param("status") DonationStatus status
    );

    List<Donation> findByStatusOrderByEventTimestampDesc(DonationStatus status);

    @Query("SELECT d FROM Donation d WHERE d.donor.userId = :userId ORDER BY d.eventTimestamp DESC")
    List<Donation> findByUserIdOrderByEventTimestampDesc(@Param("userId") UUID userId);

    List<Donation> findByStatus(DonationStatus status);

    List<Donation> findByStatusIn(List<DonationStatus> statuses);
}
