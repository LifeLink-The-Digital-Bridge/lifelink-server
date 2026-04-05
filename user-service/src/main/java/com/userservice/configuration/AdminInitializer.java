package com.userservice.configuration;

import com.userservice.enums.RoleType;
import com.userservice.model.Role;
import com.userservice.model.User;
import com.userservice.model.UserRole;
import com.userservice.repository.RoleRepository;
import com.userservice.repository.UserRepository;
import com.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        String adminEmail = "admin@lifelink.com";
        
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        
        if (existingAdmin.isPresent()) {
            log.info("Default admin user already exists");
            return;
        }

        try {
            User adminUser = new User();
            adminUser.setName("System Administrator");
            adminUser.setEmail(adminEmail);
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("Admin@123"));
            adminUser.setPhone("9999999999");
            adminUser.setDob(LocalDate.of(1990, 1, 1));
            adminUser.setGender("Other");
            adminUser.setProfileImageUrl("https://ui-avatars.com/api/?name=Admin&background=EF4444&color=fff");
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());
            
            User savedAdmin = userRepository.save(adminUser);

            Role adminRole = roleRepository.findByName(RoleType.ADMIN).orElseGet(() -> {
                Role role = new Role();
                role.setName(RoleType.ADMIN);
                return roleRepository.save(role);
            });

            UserRole userRole = new UserRole();
            userRole.setUser(savedAdmin);
            userRole.setRole(adminRole);
            userRole.setAssignedAt(LocalDateTime.now());
            
            userRoleRepository.save(userRole);

            log.info("Default admin user created successfully with email: {}", adminEmail);
            log.info("IMPORTANT: Change the default admin password after first login!");
            
        } catch (Exception e) {
            log.error("Failed to create default admin user: {}", e.getMessage());
        }
    }
}
