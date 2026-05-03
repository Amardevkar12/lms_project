package com.lms.service;

import com.lms.dto.DashboardStats;
import com.lms.dto.LeaveApplyRequest;
import com.lms.dto.LeaveResponse;
import com.lms.dto.LeaveStatusUpdateRequest;
import com.lms.entity.LeaveRequest;
import com.lms.entity.LeaveStatus;
import com.lms.entity.User;
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
import com.lms.entity.Role;

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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
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
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(leave);

        List<User> managers = userRepository.findByRole(Role.MANAGER);

        for (User manager : managers) {
            emailService.sendEmail(
                    manager.getEmail(),
                    "📌 New Leave Request",
                    "Employee: " + employee.getName() +
                            "\nEmail: " + employee.getEmail() +
                            "\nFrom: " + request.getStartDate() +
                            "\nTo: " + request.getEndDate() +
                            "\nReason: " + request.getReason());
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

        // deduct leaves
        User employee = leave.getEmployee();

        int newLeaves = employee.getUsedLeaves() + (int) leave.getNumberOfDays();
        employee.setUsedLeaves(newLeaves);

        userRepository.save(employee);

        LeaveRequest saved = leaveRequestRepository.save(leave);

        // EMAIL
        emailService.sendEmail(
        employee.getEmail(),
        "Leave Approved - Good News!",
        "Dear " + employee.getName() + ",\n\n" +
        "Good news! Your leave request has been APPROVED.\n\n" +
        "Please make sure to complete or handover your pending tasks before your leave starts.\n\n" +
        "Wishing you a smooth time off.\n\n" +
        "Regards,\n" +
        "HR Team"
);


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

        // EMAIL
        String reason = (request != null && request.getRejectionReason() != null)
        ? request.getRejectionReason()
        : "Not specified";

emailService.sendEmail(
        leave.getEmployee().getEmail(),
        "Leave Request Update",
        "Dear " + leave.getEmployee().getName() + ",\n\n" +
        "We regret to inform you that your leave request has NOT been approved.\n\n" +
        "⚠ REASON ⚠\n" +
        "----------------------------------\n" +
        reason + "\n" +
        "----------------------------------\n\n" +
        "If you need any clarification, please contact your manager or HR team.\n\n" +
        "Regards,\n" +
        "HR Team"
);

        return LeaveResponse.fromEntity(saved);
    }

    // ================= MY LEAVES =================
    public List<LeaveResponse> getMyLeaves() {
        User employee = getCurrentUser();

        return leaveRequestRepository.findByEmployeeOrderByCreatedAtDesc(employee)
                .stream()
                .map(LeaveResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ================= ALL LEAVES =================
    public List<LeaveResponse> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(LeaveResponse::fromEntity)
                .collect(Collectors.toList());
    }

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