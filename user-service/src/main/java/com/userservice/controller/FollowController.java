package com.userservice.controller;

import com.userservice.dto.UserDTO;
import com.userservice.service.follow.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followUser(
            @PathVariable UUID id,
            @RequestHeader("id") String followerIdHeader) {
        UUID followerId = UUID.fromString(followerIdHeader);
        if (followerId.equals(id)) {
            return ResponseEntity.badRequest().body("Cannot follow yourself");
        }
        boolean followed = followService.followUser(followerId, id);
        if (followed) return ResponseEntity.ok("Followed successfully");
        else return ResponseEntity.badRequest().body("Already following or invalid user");
    }

    @DeleteMapping("/{id}/unfollow")
    public ResponseEntity<?> unfollowUser(
            @PathVariable UUID id,
            @RequestHeader("id") String followerIdHeader) {
        UUID followerId = UUID.fromString(followerIdHeader);
        boolean unfollowed = followService.unfollowUser(followerId, id);
        if (unfollowed) return ResponseEntity.ok("Unfollowed successfully");
        else return ResponseEntity.badRequest().body("Not following or invalid user");
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getFollowers(id));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getFollowing(id));
    }

    @GetMapping("/{id}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getFollowersCount(id));
    }

    @GetMapping("/{id}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable UUID id) {
        return ResponseEntity.ok(followService.getFollowingCount(id));
    }

}
