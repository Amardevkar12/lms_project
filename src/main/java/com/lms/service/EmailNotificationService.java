package com.lms.service;

import com.lms.config.MailProperties;
import com.lms.entity.LeaveRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Async
    public void sendLeaveApprovedEmail(LeaveRequest leaveRequest) {
        try {
            sendEmail(
                    leaveRequest,
                    "Your leave request has been approved",
                    buildApprovalEmailBody(leaveRequest),
                    "approval"
            );
            log.info("Approval email sent for leave request {}", leaveRequest.getId());
        } catch (MailAuthenticationException ex) {
            log.error("Mail authentication failed while sending approval email for leave request {}", leaveRequest.getId());
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send approval email for leave request {}", leaveRequest.getId(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error while sending approval email for leave request {}", leaveRequest.getId(), ex);
        }
    }

    @Async
    public void sendLeaveRejectedEmail(LeaveRequest leaveRequest) {
        try {
            sendEmail(
                    leaveRequest,
                    "Your leave request has been rejected",
                    buildRejectionEmailBody(leaveRequest),
                    "rejection"
            );
            log.info("Rejection email sent for leave request {}", leaveRequest.getId());
        } catch (MailAuthenticationException ex) {
            log.error("Mail authentication failed while sending rejection email for leave request {}", leaveRequest.getId());
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send rejection email for leave request {}", leaveRequest.getId(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error while sending rejection email for leave request {}", leaveRequest.getId(), ex);
        }
    }

    private String buildApprovalEmailBody(LeaveRequest leaveRequest) {
        String employeeName = HtmlUtils.htmlEscape(leaveRequest.getEmployee().getName());
        String managerName = leaveRequest.getReviewedBy() != null
                ? HtmlUtils.htmlEscape(leaveRequest.getReviewedBy().getName())
                : "your manager";
        String leaveType = HtmlUtils.htmlEscape(leaveRequest.getLeaveType().name());
        String reason = HtmlUtils.htmlEscape(leaveRequest.getReason());

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937; line-height: 1.6;">
                  <h2 style="color: #166534;">Leave Approved</h2>
                  <p>Hello %s,</p>
                  <p>Your leave request has been approved by %s.</p>
                  <table style="border-collapse: collapse; margin-top: 12px;">
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Leave type</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Start date</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>End date</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Total days</strong></td><td>%d</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Reason</strong></td><td>%s</td></tr>
                  </table>
                  <p style="margin-top: 16px;">Regards,<br/>%s</p>
                </body>
                </html>
                """.formatted(
                employeeName,
                managerName,
                leaveType,
                formatDate(leaveRequest.getStartDate()),
                formatDate(leaveRequest.getEndDate()),
                leaveRequest.getNumberOfDays(),
                reason,
                HtmlUtils.htmlEscape(mailProperties.getFromName())
        );
    }

    private String buildRejectionEmailBody(LeaveRequest leaveRequest) {
        String employeeName = HtmlUtils.htmlEscape(leaveRequest.getEmployee().getName());
        String managerName = leaveRequest.getReviewedBy() != null
                ? HtmlUtils.htmlEscape(leaveRequest.getReviewedBy().getName())
                : "your manager";
        String leaveType = HtmlUtils.htmlEscape(leaveRequest.getLeaveType().name());
        String reason = HtmlUtils.htmlEscape(leaveRequest.getReason());
        String rejectionReason = isBlank(leaveRequest.getRejectionReason())
                ? "No specific reason was provided."
                : HtmlUtils.htmlEscape(leaveRequest.getRejectionReason());

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937; line-height: 1.6;">
                  <h2 style="color: #b91c1c;">Leave Rejected</h2>
                  <p>Hello %s,</p>
                  <p>Your leave request has been rejected by %s.</p>
                  <table style="border-collapse: collapse; margin-top: 12px;">
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Leave type</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Start date</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>End date</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Total days</strong></td><td>%d</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Reason</strong></td><td>%s</td></tr>
                    <tr><td style="padding: 6px 12px 6px 0;"><strong>Manager note</strong></td><td>%s</td></tr>
                  </table>
                  <p style="margin-top: 16px;">Regards,<br/>%s</p>
                </body>
                </html>
                """.formatted(
                employeeName,
                managerName,
                leaveType,
                formatDate(leaveRequest.getStartDate()),
                formatDate(leaveRequest.getEndDate()),
                leaveRequest.getNumberOfDays(),
                reason,
                rejectionReason,
                HtmlUtils.htmlEscape(mailProperties.getFromName())
        );
    }

    private void sendEmail(LeaveRequest leaveRequest, String subject, String htmlBody, String emailType)
            throws MessagingException, UnsupportedEncodingException {
        if (!mailProperties.isEnabled()) {
            log.info("Email notifications are disabled. Skipping {} email for leave request {}", emailType, leaveRequest.getId());
            return;
        }

        if (isBlank(mailProperties.getFromAddress())) {
            log.warn("Email notifications are enabled but app.mail.from-address is missing. Skipping {} email for leave request {}.",
                    emailType, leaveRequest.getId());
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(leaveRequest.getEmployee().getEmail());
        helper.setFrom(mailProperties.getFromAddress(), mailProperties.getFromName());
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private String formatDate(java.time.LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
