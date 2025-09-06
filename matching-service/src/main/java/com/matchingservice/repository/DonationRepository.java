package com.matchingservice.repository;

import com.matchingservice.model.donor.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {



    List<Donation> findByStatus(com.matchingservice.enums.DonationStatus status);

    List<Donation> findByStatusOrderByDonationDateDesc(com.matchingservice.enums.DonationStatus status);

    List<Donation> findByBloodType(com.matchingservice.enums.BloodType bloodType);

    List<Donation> findByStatusAndBloodType(
            com.matchingservice.enums.DonationStatus status,
            com.matchingservice.enums.BloodType bloodType
    );

    List<Donation> findByDonationType(com.matchingservice.enums.DonationType donationType);

    List<Donation> findByStatusAndDonationType(
            com.matchingservice.enums.DonationStatus status,
            com.matchingservice.enums.DonationType donationType
    );

    List<Donation> findByDonationDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT d FROM Donation d WHERE d.donationDate >= :thirtyDaysAgo ORDER BY d.donationDate DESC")
    List<Donation> findRecentDonations(@Param("thirtyDaysAgo") LocalDate thirtyDaysAgo);

    long countByStatus(com.matchingservice.enums.DonationStatus status);

    @Query("SELECT d FROM Donation d WHERE d.status = 'PENDING' ORDER BY d.donationDate ASC")
    List<Donation> findPendingDonationsOrderedByDate();
}
