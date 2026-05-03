package com.lms.service;

import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.entity.User;
import com.lms.exception.BadRequestException;
import com.lms.repository.UserRepository;
import com.lms.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;   // 🔥 IMPORTANT FIX

    // ================= REGISTER =================
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String normalizedContactNumber = normalizeIndianContactNumber(request.getContactNumber());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setContactNumber(normalizedContactNumber);
        user.setRole(request.getRole());
        user.setDepartment(request.getDepartment());

        // 🔥 EMAIL VERIFICATION
        user.setEnabled(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));

        user.setTotalLeaves(20);
        user.setUsedLeaves(0);

        User savedUser = userRepository.save(user);

        // 🔥 SEND EMAIL
        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        // optional JWT (after register)
        String jwt = jwtUtil.generateToken(savedUser);

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getDepartment()
        );
    }

    // ================= LOGIN =================
    public AuthResponse login(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BadRequestException("Invalid email or password");
    }

    if (!user.isEnabled()) {
        throw new BadRequestException("Please verify your email first");
    }

    String token = jwtUtil.generateToken(user);

    return new AuthResponse(
            token,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getDepartment()
    );
}

    // ================= CONTACT NORMALIZE =================
    private String normalizeIndianContactNumber(String contactNumber) {

        String digitsOnly = contactNumber.replaceAll("\\D", "");

        if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
            return digitsOnly.substring(2);
        }

        if (digitsOnly.length() == 11 && digitsOnly.startsWith("0")) {
            return digitsOnly.substring(1);
        }

        return digitsOnly;
    }
}