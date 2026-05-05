package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.dto.DashboardData;
import com.lms.entity.Employee;
import com.lms.entity.LeaveRequest; // ✅ FIXED
import com.lms.service.AdminService;
import com.lms.service.EmployeeService;
import com.lms.service.ReportService;
import com.lms.entity.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

import com.lms.service.EmailService;

import java.util.Map;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = { "http://localhost:*", "http://127.0.0.1:*", "https://lms-frotend.vercel.app",
                "https://*.vercel.app" })
public class AdminController {

        private final AdminService adminService;

        private final EmployeeService employeeService;
        private final EmailService emailService;
        private final ReportService reportService;

        public AdminController(AdminService adminService, EmployeeService employeeService, EmailService emailService,
                        ReportService reportService) {
                this.adminService = adminService;
                this.employeeService = employeeService;
                this.emailService = emailService;
                this.reportService = reportService;
        }

        // ================= DASHBOARD =================
        @GetMapping("/dashboard")
        public ResponseEntity<ApiResponse> getDashboard() {
                DashboardData data = adminService.getDashboardData();
                return ResponseEntity.ok(new ApiResponse(true, "Dashboard data fetched", data));
        }

        // ================= LEAVES =================

        // ✅ GET ALL LEAVES (FROM leave_requests TABLE)
        @GetMapping("/leaves")
        public ResponseEntity<ApiResponse> getAllLeaves() {
                return ResponseEntity.ok(
                                new ApiResponse(true, "All leaves", adminService.getAllLeaves()));
        }

        // ✅ APPLY LEAVE (OPTIONAL - mostly employee side use karega)
        @PostMapping("/leaves")
        public ResponseEntity<ApiResponse> applyLeave(@RequestBody LeaveRequest leave) {
                return ResponseEntity.status(201)
                                .body(new ApiResponse(true, "Leave applied",
                                                adminService.applyLeave(leave)));
        }

        // ✅ APPROVE LEAVE
        @PutMapping("/leaves/{id}/approve")
        public ResponseEntity<ApiResponse> approveLeave(@PathVariable Long id) {

                LeaveRequest leave = adminService.getLeaveById(id);

                LeaveRequest updated = adminService.approveLeave(id);

                // 📧 EMAIL SEND (FIXED)
                emailService.sendEmail(
                                leave.getEmployee().getEmail(),
                                "Leave Approved ✅",
                                "Hello,\n\nYour leave has been APPROVED.\n\nFrom: "
                                                + leave.getStartDate()
                                                + "\nTo: " + leave.getEndDate()
                                                + "\n\nRegards,\nHR Team");

                return ResponseEntity.ok(
                                new ApiResponse(true, "Leave approved & email sent", updated));
        }

        // ✅ REJECT LEAVE
        @PutMapping("/leaves/{id}/reject")
        public ResponseEntity<ApiResponse> rejectLeave(@PathVariable Long id) {

                LeaveRequest leave = adminService.getLeaveById(id);

                LeaveRequest updated = adminService.rejectLeave(id);

                emailService.sendEmail(
                                leave.getEmployee().getEmail(),
                                "Leave Rejected ❌",
                                "Hello,\n\nYour leave request has been REJECTED.\n\nRegards,\nHR Team");

                return ResponseEntity.ok(
                                new ApiResponse(true, "Leave rejected & email sent", updated));
        }

        // ================= EMPLOYEES =================

        @GetMapping("/employees")
        public ResponseEntity<ApiResponse> getEmployees() {
                return ResponseEntity.ok(
                                new ApiResponse(true, "All employees",
                                                employeeService.getAllEmployees()));
        }

        @GetMapping("/employees/{id}")
        public ResponseEntity<ApiResponse> getEmployeeById(@PathVariable Long id) {
                return ResponseEntity.ok(
                                new ApiResponse(true, "Employee found",
                                                employeeService.getEmployeeById(id)));
        }

        @PostMapping("/employees")
        public ResponseEntity<ApiResponse> addEmployee(@RequestBody Employee emp) {
                return ResponseEntity.status(201)
                                .body(new ApiResponse(true, "Employee added",
                                                employeeService.saveEmployee(emp)));
        }

        @PutMapping("/employees/{id}")
        public ResponseEntity<ApiResponse> updateEmployee(
                        @PathVariable Long id,
                        @RequestBody Employee emp) {

                emp.setId(id);
                return ResponseEntity.ok(
                                new ApiResponse(true, "Employee updated",
                                                employeeService.saveEmployee(emp)));
        }

        @DeleteMapping("/employees/{id}")
        public ResponseEntity<ApiResponse> deleteEmployee(@PathVariable Long id) {
                employeeService.deleteEmployee(id);
                return ResponseEntity.ok(
                                new ApiResponse(true, "Employee deleted", null));
        }

        // ================= ACTIVE EMPLOYEES =================

        @GetMapping("/active-employees")
        public ResponseEntity<ApiResponse> getActiveEmployees() {
                return ResponseEntity.ok(
                                new ApiResponse(true, "Active employees",
                                                employeeService.getActiveEmployees()));
        }

        // ================= NOTIFICATIONS =================

        @GetMapping("/notifications")
        public ResponseEntity<ApiResponse> getNotifications() {
                return ResponseEntity.ok(
                                new ApiResponse(true, "All notifications",
                                                adminService.getAllNotifications()));
        }

        @PostMapping("/notifications/mark-read/{email}")
        public ResponseEntity<ApiResponse> markAllRead(@PathVariable String email) {
                return ResponseEntity.ok(
                                new ApiResponse(true, "Marked as read",
                                                adminService.markAllAsRead(email)));
        }

        @GetMapping("/notifications/unread/{email}")
        public ResponseEntity<ApiResponse> getUnreadCount(@PathVariable String email) {
                return ResponseEntity.ok(
                                new ApiResponse(true, "Unread count",
                                                adminService.getUnreadCount(email)));
        }

        // ================= REPORTS =================

        @GetMapping("/reports")
        public ResponseEntity<ApiResponse> getReports() {
                return ResponseEntity.ok(
                                new ApiResponse(true, "Reports data",
                                                adminService.getReportData()));
        }

        // ================= CALENDAR =================

        @GetMapping("/calendar/events")
        public ResponseEntity<ApiResponse> getCalendarEvents() {

                try {

                        List<Map<String, Object>> events = adminService.getLeaveEvents();

                        if (events == null) {
                                events = new java.util.ArrayList<>();
                        }

                        return ResponseEntity.ok(
                                        new ApiResponse(true, "Calendar events fetched", events));

                } catch (Exception e) {
                        e.printStackTrace();

                        return ResponseEntity.status(500)
                                        .body(new ApiResponse(false, "Calendar API failed: " + e.getMessage(), null));
                }
        }

        @GetMapping("/notifications/user/{email}")
        public ResponseEntity<ApiResponse> getUserNotifications(@PathVariable String email) {

                return ResponseEntity.ok(
                                new ApiResponse(true, "User notifications",
                                                adminService.getNotificationsByEmail(email)));

        }

        // ================= TEST =================

        @GetMapping("/test")
        public String test() {
                return "ADMIN API WORKING";
        }

        // @GetMapping("/reports/pdf")
        // public ResponseEntity<byte[]> downloadReportPdf() {

        // byte[] pdf = adminService.generateReportPdf();

        // return ResponseEntity.ok()
        // .header("Content-Type", "application/pdf")
        // .header("Content-Disposition", "attachment; filename=report.pdf")
        // .body(pdf);
        // }

        @GetMapping("/reports/excel")
        public ResponseEntity<byte[]> downloadExcelReport() {

                byte[] excelData = reportService.generateExcel(); // ✅ FIXED

                return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=lms_report.xlsx")
                                .header("Content-Type",
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .body(excelData);
        }

        // ✅ MARK SINGLE AS READ
        @PutMapping("/notifications/read/{id}")
        public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long id) {
                return ResponseEntity.ok(
                                new ApiResponse(true, "Marked as read",
                                                adminService.markAsRead(id)));
        }

        // ✅ DELETE NOTIFICATION
        @DeleteMapping("/notifications/{id}")
        public ResponseEntity<ApiResponse> deleteNotification(@PathVariable Long id) {
                adminService.deleteNotification(id);
                return ResponseEntity.ok(
                                new ApiResponse(true, "Deleted", null));
        }

        
}
