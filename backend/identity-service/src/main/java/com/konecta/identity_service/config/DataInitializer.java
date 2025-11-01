package com.konecta.identity_service.config;

import com.konecta.identity_service.entity.Role;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@email.com")) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@email.com");
            admin.setPhone("+01234567890");
            admin.setPasswordHash(passwordEncoder.encode("password"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("hr_associate@email.com")) {
            User emp = new User();
            emp.setFirstName("HR");
            emp.setLastName("Associate");
            emp.setEmail("hr_associate@email.com");
            emp.setPhone("+01234567891");
            emp.setPasswordHash(passwordEncoder.encode("password"));
            emp.setRole(Role.ASSOCIATE);
            emp.setActive(true);
            userRepository.save(emp);
        }
        if (!userRepository.existsByEmail("hr_manager@email.com")) {
            User emp = new User();
            emp.setFirstName("HR");
            emp.setLastName("Manager");
            emp.setEmail("hr_manager@email.com");
            emp.setPhone("+01234567892");
            emp.setPasswordHash(passwordEncoder.encode("password"));
            emp.setRole(Role.MANAGER);
            emp.setActive(true);
            userRepository.save(emp);
        }
        if (!userRepository.existsByEmail("accountant@email.com")) {
            User emp = new User();
            emp.setFirstName("Accountant");
            emp.setLastName("User");
            emp.setEmail("accountant@email.com");
            emp.setPhone("+01234567893");
            emp.setPasswordHash(passwordEncoder.encode("password"));
            emp.setRole(Role.ACCOUNTANT);
            emp.setActive(true);
            userRepository.save(emp);
        }

        if (!userRepository.existsByEmail("cfo@email.com")) {
            User emp = new User();
            emp.setFirstName("CFO");
            emp.setLastName("User");
            emp.setEmail("cfo@email.com");
            emp.setPhone("+01234567894");
            emp.setPasswordHash(passwordEncoder.encode("password"));
            emp.setRole(Role.CFO);
            emp.setActive(true);
            userRepository.save(emp);
        }
    }
}