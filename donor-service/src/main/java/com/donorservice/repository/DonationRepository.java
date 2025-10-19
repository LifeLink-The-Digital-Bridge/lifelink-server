package com.donorservice.repository;

import com.donorservice.enums.DonationStatus;
import com.donorservice.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {

    List<Donation> findByDonorId(UUID donorId);

    boolean existsByDonorIdAndStatus(UUID donorId, DonationStatus status);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM Donation d WHERE d.donor.id = :donorId " +
            "AND d.status IN :statuses")
    boolean existsByDonorIdAndStatusIn(
            @Param("donorId") UUID donorId,
            @Param("statuses") List<DonationStatus> statuses
    );

    @Query("SELECT d FROM Donation d WHERE d.donor.id = :donorId " +
            "AND d.status IN :statuses")
    List<Donation> findByDonorIdAndStatusIn(
            @Param("donorId") UUID donorId,
            @Param("statuses") List<DonationStatus> statuses
    );

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.donor.id = :donorId " +
            "AND d.status IN :statuses")
    long countByDonorIdAndStatusIn(
            @Param("donorId") UUID donorId,
            @Param("statuses") List<DonationStatus> statuses
    );
}
