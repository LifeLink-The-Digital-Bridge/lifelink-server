package com.userservice.service.follow;

import com.userservice.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface FollowService {

    boolean followUser(UUID followerId, UUID followingId);

    boolean unfollowUser(UUID followerId, UUID followingId);

    boolean isFollowing(UUID followerId, UUID followingId);

    List<UserDTO> getFollowers(UUID userId);

    List<UserDTO> getFollowing(UUID userId);

    long getFollowersCount(UUID userId);

    long getFollowingCount(UUID userId);

}
