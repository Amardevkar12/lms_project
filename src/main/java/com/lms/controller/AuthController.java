package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.dto.AuthResponse;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.service.AuthService;
import com.lms.service.EmailService;
import com.lms.entity.User;
import com.lms.repository.UserRepository;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = { "http://localhost:*", "http://127.0.0.1:*" })
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    

    // ================= REGISTER (DTO BASED - MAIN) =================
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "User registered successfully", authResponse)
        );
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Login successful", authResponse)
        );
    }

    // ================= EMAIL VERIFICATION =================
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {

        User user = userRepository.findByVerificationToken(token);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid token", null));
        }

        if (user.getTokenExpiry() == null ||
                user.getTokenExpiry().isBefore(LocalDateTime.now())) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Token expired", null));
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                new ApiResponse(true, "Email verified successfully", null)
        );
    }

    // ================= OPTIONAL: SIMPLE REGISTER (if NOT using DTO) =================
    @PostMapping("/register-simple")
    public ResponseEntity<ApiResponse> registerSimple(@RequestBody User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        String token = UUID.randomUUID().toString();

        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return ResponseEntity.ok(
                new ApiResponse(true, "Check email to verify account", null)
        );
    }
}