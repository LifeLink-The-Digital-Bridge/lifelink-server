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
import com.userservice.service.follow.FollowService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowService followService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, FollowService followService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.followService = followService;
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
        BeanUtils.copyProperties(userDTO, user, "migrantDetails", "doctorDetails", "ngoDetails", "roles");
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if (userDTO.getMigrantDetails() != null) {
            com.userservice.model.MigrantDetails migrantDetails = new com.userservice.model.MigrantDetails();
            BeanUtils.copyProperties(userDTO.getMigrantDetails(), migrantDetails);
            migrantDetails.setUser(user);
            user.setMigrantDetails(migrantDetails);
        }

        if (userDTO.getDoctorDetails() != null) {
            com.userservice.model.DoctorDetails doctorDetails = new com.userservice.model.DoctorDetails();
            BeanUtils.copyProperties(userDTO.getDoctorDetails(), doctorDetails);
            doctorDetails.setUser(user);
            user.setDoctorDetails(doctorDetails);
        }

        if (userDTO.getNgoDetails() != null) {
            com.userservice.model.NGODetails ngoDetails = new com.userservice.model.NGODetails();
            BeanUtils.copyProperties(userDTO.getNgoDetails(), ngoDetails);
            ngoDetails.setUser(user);
            user.setNgoDetails(ngoDetails);
        }

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            for (String roleName : userDTO.getRoles()) {
                RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
                Role role = roleRepository.findByName(roleType)
                        .orElseGet(() -> {
                            Role newRole = new Role();
                            newRole.setName(roleType);
                            return roleRepository.save(newRole);
                        });
                
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                user.getUserRoles().add(userRole);
            }
        } else {
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
        }

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

        if (canViewProfile(profileUser, requesterId)) {
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

    @Override
    public UserDTO getUserProfileById(UUID userId, UUID requesterId) {
        User profileUser = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDTO responseDTO = new UserDTO();
        BeanUtils.copyProperties(profileUser, responseDTO);

        if (canViewProfile(profileUser, requesterId)) {
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

    private boolean canViewProfile(User profileUser, UUID requesterId) {
        if (profileUser.getProfileVisibility() == Visibility.PUBLIC) return true;
        if (profileUser.getProfileVisibility() == Visibility.PRIVATE) {
            return profileUser.getId().equals(requesterId);
        }
        if (profileUser.getProfileVisibility() == Visibility.FOLLOWERS_ONLY) {
            return requesterId != null && followService.isFollowing(requesterId, profileUser.getId());
        }
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
        
        if (userDB.getMigrantDetails() != null) {
            MigrantDetailsDTO migrantDTO = new MigrantDetailsDTO();
            BeanUtils.copyProperties(userDB.getMigrantDetails(), migrantDTO);
            responseDTO.setMigrantDetails(migrantDTO);
        }
        
        if (userDB.getDoctorDetails() != null) {
            DoctorDetailsDTO doctorDTO = new DoctorDetailsDTO();
            BeanUtils.copyProperties(userDB.getDoctorDetails(), doctorDTO);
            responseDTO.setDoctorDetails(doctorDTO);
        }
        
        if (userDB.getNgoDetails() != null) {
            NGODetailsDTO ngoDTO = new NGODetailsDTO();
            BeanUtils.copyProperties(userDB.getNgoDetails(), ngoDTO);
            responseDTO.setNgoDetails(ngoDTO);
        }
        
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

    @Override
    public List<UserDTO> searchUsers(String query, UUID requesterId) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

        return users.stream()
                .map(this::mapUserToDTO)
                .limit(20)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchDoctors() {
        return userRepository.findByDoctorDetailsIsNotNull().stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchNGOs() {
        return userRepository.findByNgoDetailsIsNotNull().stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminUserAnalyticsDTO getAdminUserAnalytics() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return new AdminUserAnalyticsDTO(
                userRepository.count(),
                userRepository.countByMigrantDetailsIsNotNull(),
                userRepository.countByDoctorDetailsIsNotNull(),
                userRepository.countByNgoDetailsIsNotNull(),
                userRepository.countByCreatedAtAfter(thirtyDaysAgo)
        );
    }

    @Override
    public List<NearbyUserDTO> searchNearbyUsers(double latitude, double longitude, double radiusKm, List<String> roles) {
        double normalizedRadius = radiusKm <= 0 ? 10.0 : Math.min(radiusKm, 100.0);
        Set<String> roleFilter = normalizeRoleFilter(roles);
        List<User> users = userRepository.findAllWithRolesAndDetails();
        List<NearbyUserDTO> results = new ArrayList<>();

        for (User user : users) {
            Set<String> userRoles = user.getUserRoles().stream()
                    .map(ur -> ur.getRole().getName().name())
                    .collect(Collectors.toSet());
            if (roleFilter.isEmpty() || userRoles.stream().noneMatch(roleFilter::contains)) {
                continue;
            }

            NearbyCoordinate coordinate = resolveCoordinate(user, roleFilter);
            if (coordinate == null) {
                continue;
            }

            double distanceKm = calculateDistanceKm(latitude, longitude, coordinate.latitude(), coordinate.longitude());
            if (distanceKm > normalizedRadius) {
                continue;
            }

            results.add(new NearbyUserDTO(
                    user.getId(),
                    user.getName(),
                    user.getUsername(),
                    coordinate.role(),
                    userRoles,
                    coordinate.latitude(),
                    coordinate.longitude(),
                    roundDistance(distanceKm),
                    user.getProfileImageUrl(),
                    coordinate.detail()
            ));
        }

        return results.stream()
                .sorted(Comparator.comparing(NearbyUserDTO::getDistanceKm))
                .limit(100)
                .collect(Collectors.toList());
    }

    private UserDTO mapUserToDTO(User userDB) {
        UserDTO responseDTO = new UserDTO();
        BeanUtils.copyProperties(userDB, responseDTO);
        responseDTO.setRoles(userDB.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toSet()));
        
        if (userDB.getMigrantDetails() != null) {
            MigrantDetailsDTO migrantDTO = new MigrantDetailsDTO();
            BeanUtils.copyProperties(userDB.getMigrantDetails(), migrantDTO);
            responseDTO.setMigrantDetails(migrantDTO);
        }
        
        if (userDB.getDoctorDetails() != null) {
            DoctorDetailsDTO doctorDTO = new DoctorDetailsDTO();
            BeanUtils.copyProperties(userDB.getDoctorDetails(), doctorDTO);
            responseDTO.setDoctorDetails(doctorDTO);
        }
        
        if (userDB.getNgoDetails() != null) {
            NGODetailsDTO ngoDTO = new NGODetailsDTO();
            BeanUtils.copyProperties(userDB.getNgoDetails(), ngoDTO);
            responseDTO.setNgoDetails(ngoDTO);
        }
        
        return responseDTO;
    }

    private Set<String> normalizeRoleFilter(List<String> roles) {
        Set<String> normalized = new HashSet<>();
        if (roles == null || roles.isEmpty()) {
            normalized.add(RoleType.DOCTOR.name());
            normalized.add(RoleType.MIGRANT.name());
            normalized.add(RoleType.NGO.name());
            return normalized;
        }

        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }
            for (String token : role.split(",")) {
                if (token == null || token.isBlank()) {
                    continue;
                }
                try {
                    normalized.add(RoleType.valueOf(token.trim().toUpperCase()).name());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return normalized;
    }

    private NearbyCoordinate resolveCoordinate(User user, Set<String> roleFilter) {
        if (user.getDoctorDetails() != null
                && user.getDoctorDetails().getLatitude() != null
                && user.getDoctorDetails().getLongitude() != null
                && roleFilter.contains(RoleType.DOCTOR.name())) {
            return new NearbyCoordinate(
                    RoleType.DOCTOR.name(),
                    user.getDoctorDetails().getLatitude(),
                    user.getDoctorDetails().getLongitude(),
                    user.getDoctorDetails().getHospitalName()
            );
        }

        if (user.getNgoDetails() != null
                && user.getNgoDetails().getLatitude() != null
                && user.getNgoDetails().getLongitude() != null
                && roleFilter.contains(RoleType.NGO.name())) {
            return new NearbyCoordinate(
                    RoleType.NGO.name(),
                    user.getNgoDetails().getLatitude(),
                    user.getNgoDetails().getLongitude(),
                    user.getNgoDetails().getOrganizationName()
            );
        }

        if (user.getMigrantDetails() != null
                && user.getMigrantDetails().getLatitude() != null
                && user.getMigrantDetails().getLongitude() != null
                && roleFilter.contains(RoleType.MIGRANT.name())) {
            return new NearbyCoordinate(
                    RoleType.MIGRANT.name(),
                    user.getMigrantDetails().getLatitude(),
                    user.getMigrantDetails().getLongitude(),
                    null
            );
        }

        return null;
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double originLat = Math.toRadians(lat1);
        double targetLat = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(originLat) * Math.cos(targetLat) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double roundDistance(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record NearbyCoordinate(String role, Double latitude, Double longitude, String detail) {
    }

    @Override
    public boolean checkFollowStatus(UUID followerId, String username) {
        User followedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return followService.isFollowing(followerId, followedUser.getId());
        }

    @Override
    public List<UserDTO> getUsersByIds(List<UUID> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

}
