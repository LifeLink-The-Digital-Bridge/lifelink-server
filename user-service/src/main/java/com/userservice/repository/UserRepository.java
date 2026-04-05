package com.userservice.repository;

import com.userservice.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNullApi;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);

    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String query, String query1);
    
    List<User> findByDoctorDetailsIsNotNull();
    
    List<User> findByNgoDetailsIsNotNull();

    long countByMigrantDetailsIsNotNull();

    long countByDoctorDetailsIsNotNull();

    long countByNgoDetailsIsNotNull();

    long countByCreatedAtAfter(LocalDateTime threshold);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role " +
            "LEFT JOIN FETCH u.migrantDetails " +
            "LEFT JOIN FETCH u.doctorDetails " +
            "LEFT JOIN FETCH u.ngoDetails")
    List<User> findAllWithRolesAndDetails();
}

