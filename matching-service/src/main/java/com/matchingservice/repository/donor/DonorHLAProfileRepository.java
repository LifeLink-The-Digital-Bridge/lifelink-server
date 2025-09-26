package com.matchingservice.repository.donor;

import com.matchingservice.model.donor.DonorHLAProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorHLAProfileRepository extends JpaRepository<DonorHLAProfile, Long> {

    @Query("SELECT dhp FROM DonorHLAProfile dhp WHERE dhp.donor.donorId = :donorId ORDER BY dhp.eventTimestamp DESC LIMIT 1")
    Optional<DonorHLAProfile> findLatestByDonorId(@Param("donorId") UUID donorId);

    @Query("SELECT dhp FROM DonorHLAProfile dhp WHERE dhp.donor.donorId = :donorId ORDER BY dhp.eventTimestamp DESC")
    List<DonorHLAProfile> findAllByDonorIdOrderByEventTimestampDesc(@Param("donorId") UUID donorId);

    @Query("""
        SELECT dhp FROM DonorHLAProfile dhp 
        WHERE dhp.hlaA1 = :hlaA1 OR dhp.hlaA2 = :hlaA1
        OR dhp.hlaB1 = :hlaB1 OR dhp.hlaB2 = :hlaB1
        ORDER BY dhp.eventTimestamp DESC
    """)
    List<DonorHLAProfile> findByHLAMarkers(
            @Param("hlaA1") String hlaA1,
            @Param("hlaB1") String hlaB1
    );
}
