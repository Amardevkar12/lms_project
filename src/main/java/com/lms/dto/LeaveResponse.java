package com.lms.dto;

import com.lms.entity.LeaveRequest;
import com.lms.entity.LeaveStatus;
import com.lms.entity.LeaveType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;
    private long numberOfDays;
    private LeaveType leaveType;
    private String reason;
    private LeaveStatus status;
    private String reviewedByName;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeaveResponse fromEntity(LeaveRequest leave) {
        LeaveResponse response = new LeaveResponse();
        response.setId(leave.getId());
        response.setEmployeeId(leave.getEmployee().getId());
        response.setEmployeeName(leave.getEmployee().getName());
        response.setEmployeeEmail(leave.getEmployee().getEmail());
        response.setDepartment(leave.getEmployee().getDepartment());
        response.setStartDate(leave.getStartDate());
        response.setEndDate(leave.getEndDate());
        response.setNumberOfDays(leave.getNumberOfDays());
        response.setLeaveType(leave.getLeaveType());
        response.setReason(leave.getReason());
        response.setStatus(leave.getStatus());
        if (leave.getReviewedBy() != null) {
            response.setReviewedByName(leave.getReviewedBy().getName());
        }
        response.setRejectionReason(leave.getRejectionReason());
        response.setCreatedAt(leave.getCreatedAt());
        response.setUpdatedAt(leave.getUpdatedAt());
        return response;
    }
}
