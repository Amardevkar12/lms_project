package com.lms.config;

import com.lms.entity.*;
import com.lms.repository.LeaveRequestRepository;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
    }

    private void seedUsers() {
        // Create Manager
        User manager = new User();
        manager.setName("Sarah Johnson");
        manager.setEmail("manager@lms.com");
        manager.setPassword(passwordEncoder.encode("password123"));
        manager.setContactNumber("9000000001");
        manager.setRole(Role.MANAGER);
        manager.setDepartment("Engineering");
        manager.setTotalLeaves(20);
        manager.setUsedLeaves(0);
        User savedManager = userRepository.save(manager);

        // Create Employees
        User emp1 = new User();
        emp1.setName("John Smith");
        emp1.setEmail("john@lms.com");
        emp1.setPassword(passwordEncoder.encode("password123"));
        emp1.setContactNumber("9000000002");
        emp1.setRole(Role.EMPLOYEE);
        emp1.setDepartment("Engineering");
        emp1.setTotalLeaves(20);
        emp1.setUsedLeaves(5);
        User savedEmp1 = userRepository.save(emp1);

        User emp2 = new User();
        emp2.setName("Emily Davis");
        emp2.setEmail("emily@lms.com");
        emp2.setPassword(passwordEncoder.encode("password123"));
        emp2.setContactNumber("9000000003");
        emp2.setRole(Role.EMPLOYEE);
        emp2.setDepartment("Marketing");
        emp2.setTotalLeaves(20);
        emp2.setUsedLeaves(3);
        User savedEmp2 = userRepository.save(emp2);

        User emp3 = new User();
        emp3.setName("Michael Brown");
        emp3.setEmail("michael@lms.com");
        emp3.setPassword(passwordEncoder.encode("password123"));
        emp3.setContactNumber("9000000004");
        emp3.setRole(Role.EMPLOYEE);
        emp3.setDepartment("Finance");
        emp3.setTotalLeaves(20);
        emp3.setUsedLeaves(2);
        User savedEmp3 = userRepository.save(emp3);

        // Seed Leave Requests
        // John - Approved leave
        LeaveRequest leave1 = new LeaveRequest();
        leave1.setEmployee(savedEmp1);
        leave1.setStartDate(LocalDate.now().minusDays(15));
        leave1.setEndDate(LocalDate.now().minusDays(11));
        leave1.setLeaveType(LeaveType.ANNUAL);
        leave1.setReason("Family vacation");
        leave1.setStatus(LeaveStatus.APPROVED);
        leave1.setReviewedBy(savedManager);
        leaveRequestRepository.save(leave1);

        // John - Pending leave
        LeaveRequest leave2 = new LeaveRequest();
        leave2.setEmployee(savedEmp1);
        leave2.setStartDate(LocalDate.now().plusDays(5));
        leave2.setEndDate(LocalDate.now().plusDays(7));
        leave2.setLeaveType(LeaveType.CASUAL);
        leave2.setReason("Personal work");
        leave2.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(leave2);

        // Emily - Approved leave
        LeaveRequest leave3 = new LeaveRequest();
        leave3.setEmployee(savedEmp2);
        leave3.setStartDate(LocalDate.now().minusDays(8));
        leave3.setEndDate(LocalDate.now().minusDays(6));
        leave3.setLeaveType(LeaveType.SICK);
        leave3.setReason("Medical appointment");
        leave3.setStatus(LeaveStatus.APPROVED);
        leave3.setReviewedBy(savedManager);
        leaveRequestRepository.save(leave3);

        // Emily - Pending leave
        LeaveRequest leave4 = new LeaveRequest();
        leave4.setEmployee(savedEmp2);
        leave4.setStartDate(LocalDate.now().plusDays(10));
        leave4.setEndDate(LocalDate.now().plusDays(12));
        leave4.setLeaveType(LeaveType.ANNUAL);
        leave4.setReason("Holiday trip");
        leave4.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(leave4);

        // Michael - Rejected leave
        LeaveRequest leave5 = new LeaveRequest();
        leave5.setEmployee(savedEmp3);
        leave5.setStartDate(LocalDate.now().plusDays(2));
        leave5.setEndDate(LocalDate.now().plusDays(4));
        leave5.setLeaveType(LeaveType.CASUAL);
        leave5.setReason("Personal matter");
        leave5.setStatus(LeaveStatus.REJECTED);
        leave5.setReviewedBy(savedManager);
        leave5.setRejectionReason("Critical project deadline. Please reschedule.");
        leaveRequestRepository.save(leave5);

        // Michael - Pending leave
        LeaveRequest leave6 = new LeaveRequest();
        leave6.setEmployee(savedEmp3);
        leave6.setStartDate(LocalDate.now().plusDays(20));
        leave6.setEndDate(LocalDate.now().plusDays(22));
        leave6.setLeaveType(LeaveType.ANNUAL);
        leave6.setReason("Annual vacation");
        leave6.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(leave6);

        System.out.println("✅ Sample data seeded successfully!");
        System.out.println("👤 Manager: manager@lms.com / password123");
        System.out.println("👤 Employee 1: john@lms.com / password123");
        System.out.println("👤 Employee 2: emily@lms.com / password123");
        System.out.println("👤 Employee 3: michael@lms.com / password123");
    }
}
