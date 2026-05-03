package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.dto.LeaveApplyRequest;
import com.lms.dto.LeaveStatusUpdateRequest;
import com.lms.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(originPatterns = { "http://localhost:*", "http://127.0.0.1:*" })
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> applyLeave(@Valid @RequestBody LeaveApplyRequest request) {
        var response = leaveService.applyLeave(request);
        return ResponseEntity.ok(new ApiResponse(true, "Leave application submitted successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> getMyLeaves() {
        var leaves = leaveService.getMyLeaves();
        return ResponseEntity.ok(new ApiResponse(true, "Leave requests fetched successfully", leaves));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> getAllLeaves() {
        var leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(new ApiResponse(true, "All leave requests fetched successfully", leaves));
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> approveLeave(@PathVariable Long id) {
        var response = leaveService.approveLeave(id);
        return ResponseEntity.ok(new ApiResponse(true, "Leave request approved successfully", response));
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> rejectLeave(@PathVariable Long id,
            @RequestBody(required = false) LeaveStatusUpdateRequest request) {
        var response = leaveService.rejectLeave(id, request);
        return ResponseEntity.ok(new ApiResponse(true, "Leave request rejected", response));
    }

    @GetMapping("/dashboard/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse> getEmployeeDashboard() {
        var stats = leaveService.getEmployeeDashboard();
        return ResponseEntity.ok(new ApiResponse(true, "Dashboard stats fetched", stats));
    }

    @GetMapping("/dashboard/manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> getManagerDashboard() {

        var stats = leaveService.getManagerDashboard();

        return ResponseEntity.ok(
                new ApiResponse(true, "Manager dashboard stats fetched", stats));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse> getLeaveBalance() {
        var stats = leaveService.getEmployeeDashboard();
        return ResponseEntity.ok(new ApiResponse(true, "Leave balance fetched", stats));
    }
}
