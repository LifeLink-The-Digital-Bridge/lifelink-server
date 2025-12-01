package com.donorservice.repository;

import com.donorservice.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonorRepository extends JpaRepository<Donor, UUID> {
    Donor findByUserId(UUID userId);

    @Query(value = "SELECT DISTINCT d.id FROM donors d " +
            "JOIN locations l ON d.id = l.donor_id " +
            "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * " +
            "cos(radians(l.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(l.latitude)))) < :radius",
            nativeQuery = true)
    List<UUID> findNearbyDonorIds(@Param("latitude") double latitude,
                                   @Param("longitude") double longitude,
                                   @Param("radius") double radius);

    default List<Donor> findNearbyDonors(double latitude, double longitude, double radius) {
        List<UUID> donorIds = findNearbyDonorIds(latitude, longitude, radius);
        return findAllById(donorIds);
    }
}
