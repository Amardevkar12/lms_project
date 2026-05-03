package com.lms.service;

import org.springframework.stereotype.Service;

import java.util.List;

import com.lms.entity.Employee;
import com.lms.entity.Leave;
import com.lms.entity.LeaveStatus;
import com.lms.repository.EmployeeRepository;
import com.lms.repository.LeaveRepository;

@Service
public class EmployeeService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeService(LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    // ================= SAVE LEAVE =================
    public Leave saveLeave(Leave leave) {

        if (leave.getStartDate() == null || leave.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (leave.getStartDate().isAfter(leave.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Only set default if not provided
        if (leave.getStatus() == null) {
            leave.setStatus(LeaveStatus.PENDING);
        }

        return leaveRepository.save(leave);
    }

    // ================= SAVE EMPLOYEE =================
    public Employee saveEmployee(Employee emp) {
        return employeeRepository.save(emp);
    }

    // ================= GET ALL =================
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByActiveTrue();
    }

    // ================= GET BY ID =================
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    // ================= DELETE =================
    public void deleteEmployee(Long id) {

        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }

        employeeRepository.deleteById(id);
    }

    // ================= GET LEAVES =================
    public List<Leave> getLeavesByEmployee(String email) {
        return leaveRepository.findByEmployeeEmail(email);
    }
}