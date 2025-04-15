package com.userservice.service;

import com.userservice.dto.ChangePasswordRequest;
import com.userservice.dto.SignUpRequest;
import com.userservice.dto.UserDTO;
import com.userservice.dto.UserDTOPassword;

import java.util.UUID;

public interface UserService {
    UserDTO registerUser(SignUpRequest userDTO);
    UserDTOPassword getUserById(UUID id);

    UserDTOPassword getUserByEmail(String email);

    UserDTOPassword getUserByUsername(String username);

    boolean updatePassword(ChangePasswordRequest changePasswordRequest);
}
