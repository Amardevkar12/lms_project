package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug/email")
public class EmailTestController {

    private final EmailService emailService;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/send")
    public ResponseEntity<ApiResponse> sendTestEmail(@RequestParam String to) {
        String subject = "LMS Test Email";
        String body = "This is a test email from LMS.\n\nTime: " + LocalDateTime.now();

        emailService.sendEmail(to, subject, body);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("from", maskEmail(mailUsername));
        data.put("to", to);
        data.put("subject", subject);
        data.put("smtpAccepted", true);

        return ResponseEntity.ok(new ApiResponse(true, "SMTP accepted test email", data));
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "not configured";
        }

        String[] parts = email.split("@", 2);
        String name = parts[0];
        String maskedName = name.length() <= 2 ? name.charAt(0) + "*" : name.substring(0, 2) + "***";
        return maskedName + "@" + parts[1];
    }
}
