package com.lms.repository;

import com.lms.entity.LeaveRequest;
import com.lms.entity.LeaveStatus;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeOrderByCreatedAtDesc(User employee);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    List<LeaveRequest> findByStatus(LeaveStatus status);

    long countByStatus(LeaveStatus status);

    long countByEmployee(User employee);

    long countByEmployeeAndStatus(User employee, LeaveStatus status);

    // ✅ FIXED QUERY (NO ERROR)
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee = :employee AND lr.status = 'APPROVED'")
    Long sumApprovedLeavesByEmployee(User employee);
}