package com.lms.service;

import com.lms.entity.Meeting;
import com.lms.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final EmailService emailService;

    public MeetingService(MeetingRepository meetingRepository, EmailService emailService) {
        this.meetingRepository = meetingRepository;
        this.emailService = emailService;
    }

    public Map<String, Object> saveMeeting(Meeting meeting) {
        Meeting saved = meetingRepository.save(meeting);

        List<String> sentEmails = new ArrayList<>();
        List<String> failedEmails = new ArrayList<>();

        if (meeting.getParticipants() != null) {
            for (String rawEmail : meeting.getParticipants()) {
                if (rawEmail == null || rawEmail.trim().isEmpty()) {
                    continue;
                }

                String email = rawEmail.trim();

                try {
                    emailService.sendEmail(
                            email,
                            "Meeting Invitation",
                            "Hello,\n\nYou are invited to a meeting.\n\n"
                                    + "Title: " + meeting.getTitle()
                                    + "\nDate: " + meeting.getMeetingDateTime()
                                    + "\nDescription: " + (meeting.getDescription() != null ? meeting.getDescription() : "")
                                    + "\n\nRegards,\nAdmin");
                    sentEmails.add(email);
                } catch (Exception e) {
                    failedEmails.add(email + " (" + e.getMessage() + ")");
                    System.out.println("MEETING EMAIL FAILED for " + email + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("meeting", saved);
        response.put("emailSent", failedEmails.isEmpty());
        response.put("sentEmails", sentEmails);
        response.put("failedEmails", failedEmails);
        response.put("message", failedEmails.isEmpty()
                ? "Meeting scheduled and emails sent"
                : "Meeting scheduled, but some emails failed");

        return response;
    }
}
