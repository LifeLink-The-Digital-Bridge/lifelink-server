package com.userservice.service;

import com.userservice.dto.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.UUID;

public interface UserService {
    UserDTO registerUser(SignUpRequest userDTO);
    UserDTOPassword getUserById(UUID id);

    UserDTOPassword getUserByEmail(String email);

    UserDTOPassword getUserByUsername(String username);

    boolean updatePassword(ChangePasswordRequest changePasswordRequest);


    boolean addRole(UUID id, String role);

    UserDTO updateUser(UUID userId, UserUpdateRequest updateRequest);

    UserDTO getUserProfile(String username, UUID uuid);
}
