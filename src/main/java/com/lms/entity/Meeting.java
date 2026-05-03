package com.lms.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String meetingDateTime;

    @ElementCollection
    private List<String> participants;

    // ✅ getters setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMeetingDateTime() { return meetingDateTime; }
    public void setMeetingDateTime(String meetingDateTime) { this.meetingDateTime = meetingDateTime; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
}