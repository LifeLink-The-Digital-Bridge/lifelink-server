package com.userservice.controller;

import com.userservice.aop.InternalOnly;
import com.userservice.dto.ChangePasswordRequest;
import com.userservice.dto.SignUpRequest;
import com.userservice.dto.UserDTO;
import com.userservice.dto.UserDTOPassword;
import com.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
}
