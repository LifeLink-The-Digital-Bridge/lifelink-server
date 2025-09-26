package com.matchingservice.repository.donor;

import com.matchingservice.model.donor.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorRepository extends JpaRepository<Donor, UUID> {

    @Query("SELECT d FROM Donor d WHERE d.donorId = :donorId ORDER BY d.eventTimestamp DESC LIMIT 1")
    Optional<Donor> findTopByDonorIdOrderByEventTimestampDesc(@Param("donorId") UUID donorId);

    List<Donor> findByDonorIdOrderByEventTimestampDesc(UUID donorId);

    @Query("SELECT d FROM Donor d WHERE d.userId = :userId ORDER BY d.eventTimestamp DESC LIMIT 1")
    Optional<Donor> findLatestByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT d FROM Donor d WHERE d.eventTimestamp = (
            SELECT MAX(d2.eventTimestamp) FROM Donor d2 WHERE d2.donorId = d.donorId
        )
    """)
    List<Donor> findAllLatestVersions();
}
