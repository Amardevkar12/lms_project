package com.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public void sendEmail(String to, String subject, String body) {
        if (mailUsername == null || mailUsername.isBlank()) {
            throw new IllegalStateException("Email username is missing. Set MAIL_USERNAME in backend environment variables.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(mailUsername);

            mailSender.send(message);
        } catch (MailAuthenticationException e) {
            throw new IllegalStateException(
                    "Email authentication failed. Use the Gmail account email as spring.mail.username and a valid Gmail App Password as spring.mail.password.",
                    e);
        } catch (MailException e) {
            throw new IllegalStateException("Email sending failed: " + e.getMessage(), e);
        }
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
