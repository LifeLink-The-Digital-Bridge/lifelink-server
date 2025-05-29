package com.userservice.service;

import com.userservice.dto.*;
import com.userservice.enums.Visibility;
import com.userservice.exception.ProvideNewPasswordException;
import com.userservice.exception.UserAlreadyExistsException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.model.Role;
import com.userservice.model.User;
import com.userservice.enums.RoleType;
import com.userservice.model.UserRole;
import com.userservice.repository.RoleRepository;
import com.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO registerUser(SignUpRequest userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + userDTO.getEmail());
        }

        if (userRepository.findByPhone(userDTO.getPhone()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with phone number: " + userDTO.getPhone());
        }

        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with username: " + userDTO.getUsername());
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Role defaultRole = roleRepository.findByName(RoleType.DEFAULT)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleType.DEFAULT);
                    return roleRepository.save(newRole);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(defaultRole);

        user.getUserRoles().add(userRole);

        return getUserDTO(user);
    }

    @Override
    public UserDTOPassword getUserById(UUID id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        UserDTOPassword responseDTO = new UserDTOPassword();
        BeanUtils.copyProperties(user, responseDTO);
        responseDTO.setRoles(user.getUserRoles().stream()
                .map(role -> role.getRole().getName().name())
                .collect(Collectors.toSet()));
        return responseDTO;
    }

    @Override
    public UserDTOPassword getUserByEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        UserDTOPassword responseDTO = new UserDTOPassword();
        BeanUtils.copyProperties(user, responseDTO);
        responseDTO.setRoles(user.getUserRoles().stream()
                .map(role -> role.getRole().getName().name())
                .collect(Collectors.toSet()));
        return responseDTO;
    }

    @Override
    public UserDTOPassword getUserByUsername(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        UserDTOPassword responseDTO = new UserDTOPassword();
        BeanUtils.copyProperties(user, responseDTO);
        responseDTO.setRoles(user.getUserRoles().stream()
                .map(role -> role.getRole().getName().name())
                .collect(Collectors.toSet()));
        return responseDTO;
    }


    @Override
    public boolean updatePassword(ChangePasswordRequest changePasswordRequest) {
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getRepeatPassword())) return false;

        User user = userRepository.findByEmail(changePasswordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + changePasswordRequest.getEmail()));
        String newEncodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());

        if (passwordEncoder.matches(changePasswordRequest.getRepeatPassword(), user.getPassword())) {
            throw new ProvideNewPasswordException("Please provide new password to change the password");
        }
        System.out.println("New password: " + newEncodedPassword);
        user.setPassword(newEncodedPassword);
        User updatedUser = userRepository.save(user);

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), updatedUser.getPassword())) {
            System.out.println("Password updated successfully for user: " + updatedUser.getUsername());
            return true;
        }
        System.out.println("Failed to update password for user: " + updatedUser.getUsername());
        return false;
    }

    public UserDTO getUserProfile(String username, UUID requesterId) {
        User profileUser = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDTO responseDTO = new UserDTO();
        BeanUtils.copyProperties(profileUser, responseDTO);

        if (shouldShowFullProfile(profileUser, requesterId)) {
            responseDTO.setRoles(profileUser.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName().name())
                    .collect(Collectors.toSet()));
        } else {
            responseDTO.setRoles(Set.of());
            responseDTO.setEmail(null);
            responseDTO.setPhone(null);
            responseDTO.setDob(null);
        }

        responseDTO.setProfileVisibility(profileUser.getProfileVisibility());
        return responseDTO;
    }

    private boolean shouldShowFullProfile(User profileUser, UUID requesterId) {
        if (profileUser.getProfileVisibility() == Visibility.PUBLIC) return true;
        if (profileUser.getProfileVisibility() == Visibility.PRIVATE) return false;
        if (requesterId == null) return false;

        return false;
    }


    @Override
    @Transactional
    public UserDTO updateUser(UUID userId, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (updateRequest.getName() != null) user.setName(updateRequest.getName());
        if (updateRequest.getPhone() != null) user.setPhone(updateRequest.getPhone());
        if (updateRequest.getDob() != null) user.setDob(updateRequest.getDob());
        if (updateRequest.getGender() != null) user.setGender(updateRequest.getGender());
        if (updateRequest.getProfileImageUrl() != null) user.setProfileImageUrl(updateRequest.getProfileImageUrl());
        if (updateRequest.getProfileVisibility() != null) user.setProfileVisibility(updateRequest.getProfileVisibility());

        return getUserDTO(user);
    }

    private UserDTO getUserDTO(User user) {
        User userDB = userRepository.save(user);
        UserDTO responseDTO = new UserDTO();
        BeanUtils.copyProperties(userDB, responseDTO);
        responseDTO.setRoles(userDB.getUserRoles().stream()
                .map(role -> role.getRole().getName().name())
                .collect(Collectors.toSet()));
        return responseDTO;
    }


    @Override
    public boolean addRole(UUID id, String roleName) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        System.out.println("After Exception Handel user: "+user);
        RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
        Role role = roleRepository.findByName(roleType)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleType);
                    return roleRepository.save(newRole);
                });

        boolean alreadyAssigned = user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName().equals(roleType));

        if (alreadyAssigned) return true;

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        user.getUserRoles().add(userRole);
        System.out.println("Role added successfully");
        System.out.println(user.getUserRoles());
        userRepository.save(user);
        return true;
    }
}
