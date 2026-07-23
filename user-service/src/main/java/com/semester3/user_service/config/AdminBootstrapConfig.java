package com.semester3.user_service.config;

import com.semester3.user_service.entity.Role;
import com.semester3.user_service.entity.User;
import com.semester3.user_service.repository.RoleRepository;
import com.semester3.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

// Seeds the ADMIN/EMPLOYEE roles and one default admin account so the class
// has a way in before any admin exists. Change/remove this for a real deployment.
@Configuration
public class AdminBootstrapConfig {

    @Bean
    public CommandLineRunner seedAdminUser(UserRepository userRepository,
                                            RoleRepository roleRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
            roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_EMPLOYEE")));
            // Needed for payroll-services, which gates endpoints on these roles
            roleRepository.findByName("ROLE_HR_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_HR_ADMIN")));
            roleRepository.findByName("ROLE_FINANCE")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_FINANCE")));

            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                admin.setRoles(roles);
                userRepository.save(admin);
            }
        };
    }
}
