package com.lms.repository;

import com.lms.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ================= GET USER NOTIFICATIONS =================
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // ================= LATEST 10 =================
    List<Notification> findTop10ByUserEmailOrderByCreatedAtDesc(String userEmail);

    // ================= UNREAD COUNT =================
    long countByUserEmailAndReadFalse(String userEmail);

    // ================= MARK ALL AS READ =================
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userEmail = :userEmail")
    int markAllAsRead(String userEmail);

    // ================= DELETE USER NOTIFICATIONS =================
    void deleteByUserEmail(String userEmail);

    List<Notification> findAllByUserEmail(String email);
}