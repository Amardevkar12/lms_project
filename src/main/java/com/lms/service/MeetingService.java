package com.lms.service;

import org.springframework.stereotype.Service;
import com.lms.entity.Meeting;
import com.lms.repository.MeetingRepository;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final EmailService emailService;

    public MeetingService(MeetingRepository meetingRepository, EmailService emailService) {
        this.meetingRepository = meetingRepository;
        this.emailService = emailService;
    }

    public Meeting saveMeeting(Meeting meeting) {

        // ✅ save in DB
        Meeting saved = meetingRepository.save(meeting);

        // ✅ EMAIL LOGIC
        if (meeting.getParticipants() != null) {
            for (String email : meeting.getParticipants()) {

                emailService.sendEmail(
                        email,
                        "Meeting Invitation 📅",
                        "Hello,\n\nYou are invited to a meeting.\n\n"
                                + "Title: " + meeting.getTitle()
                                + "\nDate: " + meeting.getMeetingDateTime()
                                + "\n\nRegards,\nAdmin");
            }
        }

        return saved;
    }
}