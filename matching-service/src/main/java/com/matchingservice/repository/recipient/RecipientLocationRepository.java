package com.matchingservice.repository.recipient;

import com.matchingservice.model.recipients.RecipientLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientLocationRepository extends JpaRepository<RecipientLocation, UUID> {

    @Query("SELECT rl FROM RecipientLocation rl WHERE rl.locationId = :locationId ORDER BY rl.eventTimestamp DESC LIMIT 1")
    Optional<RecipientLocation> findTopByLocationIdOrderByEventTimestampDesc(@Param("locationId") UUID locationId);

    @Query("""
        SELECT rl FROM RecipientLocation rl 
        WHERE rl.recipient.recipientId = :recipientId 
        AND rl.eventTimestamp = (
            SELECT MAX(rl2.eventTimestamp) FROM RecipientLocation rl2 WHERE rl2.locationId = rl.locationId
        )
        ORDER BY rl.eventTimestamp DESC
    """)
    List<RecipientLocation> findLatestLocationsByRecipientId(@Param("recipientId") UUID recipientId);

    @Query("SELECT rl FROM RecipientLocation rl WHERE rl.city = :city ORDER BY rl.eventTimestamp DESC")
    List<RecipientLocation> findByCityOrderByEventTimestampDesc(@Param("city") String city);

    @Query("""
        SELECT rl FROM RecipientLocation rl 
        WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(rl.latitude)) * 
               cos(radians(rl.longitude) - radians(:longitude)) + 
               sin(radians(:latitude)) * sin(radians(rl.latitude)))) <= :radiusKm
        ORDER BY rl.eventTimestamp DESC
    """)
    List<RecipientLocation> findLocationsWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );
}
