package com.lms.repository;

import com.lms.entity.Leave;
import com.lms.entity.LeaveStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    long countByStatus(LeaveStatus status);

    long countByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            LeaveStatus status);

    List<Leave> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            LeaveStatus status);

    List<Leave> findByEmployeeEmail(String employeeEmail);

    List<Leave> findTop5ByOrderByStartDateDesc();

    @Query("SELECT COUNT(l) FROM Leave l WHERE MONTH(l.startDate) = :month AND YEAR(l.startDate) = :year")
    long countByMonthAndYear(int month, int year);

    // ✅ Cleaner version
    @Query("SELECT COUNT(l) FROM Leave l WHERE :today BETWEEN l.startDate AND l.endDate AND l.status = :status")
    long countActiveLeaves(LocalDate today, LeaveStatus status);

    
}