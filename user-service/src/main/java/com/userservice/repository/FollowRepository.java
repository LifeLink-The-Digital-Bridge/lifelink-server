package com.userservice.repository;

import com.userservice.model.Follow;
import com.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    List<Follow> findAllByFollowerId(UUID followerId);

    List<Follow> findAllByFollowingId(UUID followingId);

    long countByFollowingId(UUID followingId);

    long countByFollowerId(UUID followerId);
}
