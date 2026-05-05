package com.lms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_EMAILS_URL = "https://api.resend.com/emails";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${app.mail.from-address:}")
    private String fromAddress;

    @Value("${app.mail.from-name:Leave Management System}")
    private String fromName;

    public void sendEmail(String to, String subject, String body) {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            sendWithResend(to, subject, body);
            return;
        }

        sendWithSmtp(to, subject, body);
    }

    private void sendWithResend(String to, String subject, String body) {
        String from = resolveFromAddress();

        try {
            Map<String, Object> payload = Map.of(
                    "from", fromName + " <" + from + ">",
                    "to", new String[] { to },
                    "subject", subject,
                    "text", body);

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_EMAILS_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Resend email failed with status "
                        + response.statusCode() + ": " + response.body());
            }

            logger.info("Email accepted by Resend. from={}, to={}, subject={}", from, to, subject);
        } catch (Exception e) {
            throw new IllegalStateException("Email sending failed through Resend: " + e.getMessage(), e);
        }
    }

    private void sendWithSmtp(String to, String subject, String body) {
        if (mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException("Email username is missing. Set MAIL_USERNAME or use RESEND_API_KEY.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(mailUsername);
            message.setReplyTo(mailUsername);

            mailSender.send(message);
            logger.info("Email accepted by SMTP. from={}, to={}, subject={}", mailUsername, to, subject);
        } catch (MailAuthenticationException e) {
            throw new IllegalStateException("Email authentication failed. Use a valid Gmail App Password.", e);
        } catch (MailException e) {
            throw new IllegalStateException("Email sending failed: " + e.getMessage(), e);
        }
    }

    private String resolveFromAddress() {
        if (fromAddress != null && !fromAddress.isBlank()) {
            return fromAddress;
        }

        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername;
        }

        return "onboarding@resend.dev";
    }

    public void sendVerificationEmail(String to, String token) {
        String link = "http://localhost:8080/api/auth/verify-email?token=" + token;
        String subject = "Verify Your LMS Account";
        String body = "Welcome to LMS\n\n" +
                "Dear User,\n\n" +
                "Thank you for registering with our Leave Management System.\n" +
                "To activate your account, please verify your email by clicking the link below:\n\n" +
                link + "\n\n" +
                "Note: This verification link is valid for only 15 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best Regards,\n" +
                "LMS Team";

        sendEmail(to, subject, body);
    }

    public void sendLeaveStatusEmail(String to, String status, String reason, String employeeName) {
        String subject = "Leave Update: " + status;
        String body = "Hello " + employeeName + ",\n\n" +
                "Your leave request has been " + status + ".\n\n" +
                "Status Details:\n" +
                "Status: " + status + "\n" +
                "Reason: " + (reason != null ? reason : "Not specified") + "\n\n" +
                "If you have any questions, please contact HR.\n\n" +
                "Regards,\n" +
                "LMS Team";

        sendEmail(to, subject, body);
    }
}
