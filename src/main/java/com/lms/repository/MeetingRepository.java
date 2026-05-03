package com.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.entity.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}