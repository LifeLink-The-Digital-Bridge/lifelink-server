package com.authservice.client;

import com.authservice.dto.ChangePasswordRequest;
import com.authservice.dto.PasswordUpdateRequest;
import com.authservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(name = "user-service", configuration = FeignClientConfig.class)
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable UUID id);

    @GetMapping("/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable String username);

    @GetMapping("/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable String email);

    @PostMapping("users/updatePassword")
    String updatePassword(ChangePasswordRequest changePasswordRequest);
}
