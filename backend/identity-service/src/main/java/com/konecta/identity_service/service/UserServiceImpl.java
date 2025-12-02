package com.konecta.identity_service.service;

import com.konecta.identity_service.config.PasswordGenerator;
import com.konecta.identity_service.dto.request.*;
import com.konecta.identity_service.dto.response.UserResponse;
import com.konecta.identity_service.entity.Role;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.exception.DuplicateResourceException;
import com.konecta.identity_service.exception.InvalidRequestException;
import com.konecta.identity_service.exception.ResourceNotFoundException;
import com.konecta.identity_service.mapper.UserMapper;
import com.konecta.identity_service.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_DURATION = 15;
    @Value("${app.rabbitmq.otp-exchange}")
    private String otpExchange;
    @Value("${app.rabbitmq.otp-routing-key}")
    private String otpRoutingKey;
    @Value("${app.rabbitmq.welcome-exchange}")
    private String welcomeExchange;
    @Value("${app.rabbitmq.welcome-routing-key}")
    private String welcomeRoutingKey;


    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordGenerator passwordGenerator, PasswordEncoder passwordEncoder, JwtService jwtService, StringRedisTemplate redisTemplate, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Duplicate entry for email " + request.getEmail(),
                    "Email already exists."
            );
        }
        if (request.getRole() == null || !request.getRole().isAssignable()) {
            throw new InvalidRequestException(
                    "Role " + request.getRole() + " is not assignable.",
                    "A valid, assignable role must be provided."
            );
        }

        String randomPassword = passwordGenerator.generate();
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(randomPassword));
        user.setActive(true);
        User savedUser = userRepository.save(user);

        EmailRequest email = EmailRequest.builder()
                .recipient(savedUser.getEmail())
                .subject("Welcome to Konecta!")
                .content("<p>Your account has been created. Here are your login details:</p>"
                        + "<p><strong>Email:</strong> " + savedUser.getEmail() + "</p>"
                        + "<p><strong>Temporary Password:</strong> " + randomPassword + "</p>"
                        + "<p>Please log in and change your password immediately.</p>")
                .build();
        rabbitTemplate.convertAndSend(welcomeExchange, welcomeRoutingKey, email);
        System.out.println("DEBUG: Welcome email message published for " + savedUser.getEmail());

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user exists with ID " + id,
                        "User not found."
                ));
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user exists with ID " + id, "User not found."
                ));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DuplicateResourceException(
                        "Email " + request.getEmail() + " is already in use.",
                        "Email already exists."
                );
            }
        }
        userMapper.updateUserFromDto(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("No user exists with ID " + id, "User not found.");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<String> getAllRoles() {
        return Arrays.stream(Role.values())
                .filter(Role::isAssignable)
                .map(Enum::name)
                .toList();
    }

    @Override
    public UserResponse assignRoleToUser(UUID id, Role role) {
        if (!role.isAssignable()) {
            throw new InvalidRequestException(
                    "Role " + role.name() + " is not an assignable role.",
                    "Invalid role specified."
            );
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse revokeRoleFromUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        if (user.getRole() == null) {
            throw new InvalidRequestException(
                    "User does not have a high-level role to revoke.",
                    "User is already a base employee."
            );
        }
        user.setRole(null);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse activateUserById(UUID id) {
        return setUserActiveStatus(id, true);
    }

    @Override
    public UserResponse deactivateUserById(UUID id) {
        return setUserActiveStatus(id, false);
    }

    @Override
    public void updatePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Old password does not match.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse setUserActiveStatus(UUID id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public void generateOtp(ForgetPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with email " + request.getEmail(),
                        "Email not found."));

        String otp = String.format("%06d", secureRandom.nextInt((int) Math.pow(10, OTP_LENGTH)));
        redisTemplate.opsForValue().set("otp:" + request.getEmail(), otp, Duration.ofMinutes(15));

        EmailRequest emailRequest = EmailRequest.builder()
                .recipient(request.getEmail())
                .subject("Konecta Password Reset OTP")
                .content("<p>Your request for a One-Time Password (OTP) has been processed. Please use the code below to reset your password:</p>\n" +
                            "<div class=\"otp-block\">" + otp +"</div>\n" +
                            "<p style=\"text-align: center;\">This OTP will expire in <span class=\"expiry-time\">"+
                            OTP_DURATION  +" minutes</span>.</p>")
                .build();

        rabbitTemplate.convertAndSend(otpExchange, otpRoutingKey, emailRequest);
        System.out.println("DEBUG: OTP for " + request.getEmail() + " is " + otp);
    }

    @Override
    public String getPasswordResetToken(VerifyOtpRequest request) {
        String redisKey = "otp:" + request.getEmail();
        String storedOtp = redisTemplate.opsForValue().get(redisKey);
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new InvalidRequestException("Invalid or expired Redis key otp:" + request.getEmail(), "Invalid or expired OTP");
        }
        redisTemplate.delete(redisKey);

        return jwtService.generatePasswordResetToken(request.getEmail());
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with email " + email,
                        "User not found."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String createSeedUser(Map<String, String> request) {
        User user = userRepository.findByEmail(request.get("email")).orElse(new User());

        user.setEmail(request.get("email"));
        user.setPhone(request.get("phone"));
        user.setFirstName(request.get("firstName"));
        user.setLastName(request.get("lastName"));
        user.setRole(Role.valueOf(request.get("role")));
        user.setPasswordHash(passwordEncoder.encode(request.get("password")));
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return savedUser.getId().toString();
    }
}