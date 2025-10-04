package com.userservice.controller;

import com.userservice.aop.InternalOnly;
import com.userservice.dto.*;
import com.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        UserDTO createdUser = userService.registerUser(signUpRequest);
        return ResponseEntity.ok(createdUser);
    }

    @InternalOnly
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTOPassword> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @InternalOnly
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTOPassword> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @InternalOnly
    @GetMapping("/{id}")
    public ResponseEntity<UserDTOPassword> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest updateRequest,
            @RequestHeader("id") String userIdHeader) {

        if (!id.toString().equals(userIdHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own profile");
        }

        UserDTO responseUser = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(responseUser);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserDTO> getUserProfile(
            @PathVariable String username,
            @RequestHeader(value = "id", required = false) String requesterId) {

        UserDTO profile = userService.getUserProfile(username, requesterId != null ? UUID.fromString(requesterId) : null);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/id/{userId}")
    public ResponseEntity<UserDTO> getUserProfileById(
            @PathVariable UUID userId,
            @RequestHeader(value = "id", required = false) String requesterId) {

        UserDTO profile = userService.getUserProfileById(userId, requesterId != null ? UUID.fromString(requesterId) : null);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String query,
            @RequestHeader(value = "id", required = false) String requesterId) {

        UUID requesterUUID = requesterId != null ? UUID.fromString(requesterId) : null;
        List<UserDTO> results = userService.searchUsers(query, requesterUUID);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/profile/{username}/follow-status")
    public ResponseEntity<Boolean> checkFollowStatus(
            @PathVariable String username,
            @RequestHeader("id") String followerIdHeader) {

        UUID followerId = UUID.fromString(followerIdHeader);
        boolean isFollowing = userService.checkFollowStatus(followerId, username);
        return ResponseEntity.ok(isFollowing);
    }

    @Value("${internal.access-token}")
    private String expectedSecret;

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        System.out.println("In updatePassword method");
        boolean val = userService.updatePassword(changePasswordRequest);
        if (!val) {
            return ResponseEntity.badRequest().body("Password mismatch or user not found");
        }
        return ResponseEntity.ok("Password updated successfully");
    }

    @InternalOnly
    @PutMapping("/{id}/add-role")
    public ResponseEntity<String> addRoleToUser(@PathVariable UUID id, @RequestParam String role) {

        System.out.println("In addRoleToUser method");
        System.out.println("Id :"+ id);
        System.out.println("Role :"+ role);
        boolean val = userService.addRole(id, role);

        if (val) {
            return ResponseEntity.ok("Role added successfully");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User Role not added");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestHeader("id") String id,
                                       @RequestHeader("email") String email,
                                       @RequestHeader("roles") String roles,
                                       @RequestHeader("username") String username) {
        System.out.println("Test endpoint called");
        System.out.println("ID: " + id);
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Roles: " + roles);
        return ResponseEntity.ok("Test endpoint is working");
    }

    @GetMapping("/sample")
    public ResponseEntity<String> sample() {
        System.out.println("Sample endpoint called");
        return ResponseEntity.ok("Sample endpoint is working");
    }
}
