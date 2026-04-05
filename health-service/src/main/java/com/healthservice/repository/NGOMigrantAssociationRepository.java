package com.healthservice.repository;

import com.healthservice.model.NGOMigrantAssociation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NGOMigrantAssociationRepository extends JpaRepository<NGOMigrantAssociation, UUID> {

    Optional<NGOMigrantAssociation> findByNgoIdAndMigrantHealthId(UUID ngoId, String migrantHealthId);

    List<NGOMigrantAssociation> findByNgoIdAndIsActiveTrue(UUID ngoId);

    Page<NGOMigrantAssociation> findByNgoIdAndIsActiveTrue(UUID ngoId, Pageable pageable);

    List<NGOMigrantAssociation> findByMigrantHealthIdAndIsActiveTrue(String migrantHealthId);

    List<NGOMigrantAssociation> findByMigrantUserIdAndIsActiveTrue(UUID migrantUserId);

    List<NGOMigrantAssociation> findByNgoIdAndStatusAndIsActiveTrue(UUID ngoId, String status);

    List<NGOMigrantAssociation> findByNgoIdAndSupportTypeAndIsActiveTrue(UUID ngoId, String supportType);

    @Query("SELECT COUNT(n) FROM NGOMigrantAssociation n WHERE n.ngoId = :ngoId AND n.isActive = true")
    long countActiveMigrantsForNGO(@Param("ngoId") UUID ngoId);

    long countByIsActiveTrue();

    boolean existsByNgoIdAndMigrantHealthId(UUID ngoId, String migrantHealthId);
}
