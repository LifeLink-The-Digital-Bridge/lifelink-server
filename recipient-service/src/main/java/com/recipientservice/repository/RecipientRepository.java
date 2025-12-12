package com.recipientservice.repository;

import com.recipientservice.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, UUID> {
    Recipient findByUserId(UUID userId);

    @Query(value = "SELECT DISTINCT r.id FROM recipients r " +
            "JOIN locations l ON r.id = l.recipient_id " +
            "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * " +
            "cos(radians(l.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(l.latitude)))) < :radius",
            nativeQuery = true)
    List<UUID> findNearbyRecipientIds(@Param("latitude") double latitude,
                                       @Param("longitude") double longitude,
                                       @Param("radius") double radius);

    default List<Recipient> findNearbyRecipients(double latitude, double longitude, double radius) {
        List<UUID> recipientIds = findNearbyRecipientIds(latitude, longitude, radius);
        return findAllById(recipientIds);
    }
}
