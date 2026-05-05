package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.LeaveRequestRepository;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ================= CURRENT USER =================
    private User getCurrentUser() {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("User not authenticated");
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ================= APPLY LEAVE =================
    @Transactional
    public LeaveResponse applyLeave(LeaveApplyRequest request) {

        User employee = getCurrentUser();

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setEmployeeEmail(employee.getEmail());
        leave.setFromDate(request.getStartDate());
        leave.setToDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leave);

        // ================= EMAIL (SAFE) =================
        try {
            List<User> managers = userRepository.findByRole(Role.MANAGER);

            for (User manager : managers) {
                emailService.sendEmail(
                        manager.getEmail(),
                        "📌 New Leave Request",
                        "Employee: " + employee.getName() +
                                "\nEmail: " + employee.getEmail() +
                                "\nFrom: " + request.getStartDate() +
                                "\nTo: " + request.getEndDate() +
                                "\nReason: " + request.getReason()
                );
            }
        } catch (Exception e) {
            System.out.println("EMAIL FAILED (ignored): " + e.getMessage());
        }

        return LeaveResponse.fromEntity(saved);
    }

    // ================= APPROVE LEAVE =================
    @Transactional
    public LeaveResponse approveLeave(Long id) {

        User manager = getCurrentUser();

        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Already processed");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(manager);

        User employee = leave.getEmployee();

        int days = (int) leave.getNumberOfDays();

        employee.setUsedLeaves(employee.getUsedLeaves() + days);

        userRepository.save(employee);

        LeaveRequest saved = leaveRequestRepository.save(leave);

        // EMAIL SAFE
        try {
            emailService.sendEmail(
                    employee.getEmail(),
                    "Leave Approved",
                    "Dear " + employee.getName() + ", your leave is APPROVED."
            );
        } catch (Exception e) {
            System.out.println("EMAIL FAILED (ignored)");
        }

        return LeaveResponse.fromEntity(saved);
    }

    // ================= REJECT LEAVE =================
    @Transactional
    public LeaveResponse rejectLeave(Long id, LeaveStatusUpdateRequest request) {

        User manager = getCurrentUser();

        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Already processed");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(manager);

        if (request != null && request.getRejectionReason() != null) {
            leave.setRejectionReason(request.getRejectionReason());
        }

        LeaveRequest saved = leaveRequestRepository.save(leave);

        try {
            emailService.sendEmail(
                    leave.getEmployee().getEmail(),
                    "Leave Rejected",
                    "Your leave was rejected. Reason: " +
                            (request != null ? request.getRejectionReason() : "Not specified")
            );
        } catch (Exception e) {
            System.out.println("EMAIL FAILED (ignored)");
        }

        return LeaveResponse.fromEntity(saved);
    }

    // ================= MY LEAVES =================
    public List<LeaveResponse> getMyLeaves() {
        return leaveRequestRepository.findByEmployeeOrderByCreatedAtDesc(getCurrentUser())
                .stream().map(LeaveResponse::fromEntity).collect(Collectors.toList());
    }

    // ================= ALL LEAVES =================
    public List<LeaveResponse> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(LeaveResponse::fromEntity).collect(Collectors.toList());
    }

    // ================= DASHBOARD =================
    public DashboardStats getEmployeeDashboard() {

        User employee = getCurrentUser();

        DashboardStats stats = new DashboardStats();

        stats.setTotalLeaves(employee.getTotalLeaves());
        stats.setUsedLeaves(employee.getUsedLeaves());
        stats.setRemainingLeaves(employee.getRemainingLeaves());

        stats.setPendingRequests(
                leaveRequestRepository.countByEmployeeAndStatus(employee, LeaveStatus.PENDING));

        stats.setApprovedRequests(
                leaveRequestRepository.countByEmployeeAndStatus(employee, LeaveStatus.APPROVED));

        stats.setRejectedRequests(
                leaveRequestRepository.countByEmployeeAndStatus(employee, LeaveStatus.REJECTED));

        return stats;
    }

    public DashboardStats getManagerDashboard() {

        DashboardStats stats = new DashboardStats();

        stats.setTotalRequests(leaveRequestRepository.count());
        stats.setTotalPending(leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        stats.setApprovedRequests(leaveRequestRepository.countByStatus(LeaveStatus.APPROVED));
        stats.setRejectedRequests(leaveRequestRepository.countByStatus(LeaveStatus.REJECTED));

        return stats;
    }
}
