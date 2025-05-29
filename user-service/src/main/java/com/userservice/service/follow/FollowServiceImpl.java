package com.userservice.service.follow;

import com.userservice.dto.UserDTO;
import com.userservice.exception.UserNotFoundException;
import com.userservice.model.Follow;
import com.userservice.model.User;
import com.userservice.repository.FollowRepository;
import com.userservice.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;

    private final UserRepository userRepository;

    public FollowServiceImpl(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean followUser(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) return false; // Cannot follow self
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) return false;

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("User to follow not found"));

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
        return true;
    }

    @Override
    public boolean unfollowUser(UUID followerId, UUID followingId) {
        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        if (follow.isPresent()) {
            followRepository.delete(follow.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public List<UserDTO> getFollowers(UUID userId) {
        List<Follow> follows = followRepository.findAllByFollowingId(userId);
        return follows.stream()
                .map(f -> {
                    UserDTO dto = new UserDTO();
                    BeanUtils.copyProperties(f.getFollower(), dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<UserDTO> getFollowing(UUID userId) {
        List<Follow> follows = followRepository.findAllByFollowerId(userId);
        return follows.stream()
                .map(f -> {
                    UserDTO dto = new UserDTO();
                    BeanUtils.copyProperties(f.getFollowing(), dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public long getFollowersCount(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }

}
