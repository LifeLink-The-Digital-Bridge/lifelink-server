package com.matchingservice.repository.donor;

import com.matchingservice.model.donor.DonorLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorLocationRepository extends JpaRepository<DonorLocation, Long> {

    List<DonorLocation> findByDonor_DonorIdOrderByEventTimestampDesc(UUID donorId);

    Optional<DonorLocation> findTopByLocationIdOrderByEventTimestampDesc(UUID locationId);

    @Query("""
        SELECT dl FROM DonorLocation dl 
        WHERE dl.donor.donorId = :donorId 
        AND dl.eventTimestamp = (
            SELECT MAX(dl2.eventTimestamp) FROM DonorLocation dl2 WHERE dl2.locationId = dl.locationId
        )
        ORDER BY dl.eventTimestamp DESC
    """)
    List<DonorLocation> findLatestLocationsByDonorId(@Param("donorId") UUID donorId);

    @Query("SELECT dl FROM DonorLocation dl WHERE dl.city = :city ORDER BY dl.eventTimestamp DESC")
    List<DonorLocation> findByCityOrderByEventTimestampDesc(@Param("city") String city);

    @Query("""
        SELECT dl FROM DonorLocation dl 
        WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(dl.latitude)) * 
               cos(radians(dl.longitude) - radians(:longitude)) + 
               sin(radians(:latitude)) * sin(radians(dl.latitude)))) <= :radiusKm
        ORDER BY dl.eventTimestamp DESC
    """)
    List<DonorLocation> findLocationsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );
}
