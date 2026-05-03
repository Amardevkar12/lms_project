package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.entity.Employee;
import com.lms.entity.Leave;
import com.lms.entity.LeaveStatus;
import com.lms.service.EmployeeService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ================= GET ALL EMPLOYEES =================
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(new ApiResponse(true, "Employees fetched", employees));
    }

    // ================= ADD EMPLOYEE =================
    @PostMapping("/employees")
    public ResponseEntity<ApiResponse> addEmployee(@RequestBody Employee employee) {
        Employee saved = employeeService.saveEmployee(employee);
        return ResponseEntity.status(201)
                .body(new ApiResponse(true, "Employee added", saved));
    }

    // ================= APPLY LEAVE =================
    @PostMapping("/leaves")
    public ResponseEntity<ApiResponse> applyLeave(@RequestBody Leave leave, Authentication auth) {

        String email = auth.getName();

        leave.setEmployeeEmail(email);
        leave.setStatus(LeaveStatus.PENDING);

        Leave saved = employeeService.saveLeave(leave);

        return ResponseEntity.status(201)
                .body(new ApiResponse(true, "Leave applied", saved));
    }

    // ================= GET MY LEAVES =================
    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse> getMyLeaves(Authentication auth) {

        String email = auth.getName();

        List<Leave> leaves = employeeService.getLeavesByEmployee(email);

        return ResponseEntity.ok(new ApiResponse(true, "Employee leaves", leaves));
    }
}