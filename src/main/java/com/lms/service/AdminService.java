package com.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import com.itextpdf.kernel.pdf.PdfWriter;
// import com.itextpdf.kernel.pdf.PdfDocument;
// import com.itextpdf.layout.Document;
// import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;

import com.lms.dto.DashboardData;
import com.lms.dto.ReportData;
import com.lms.entity.Employee;
import com.lms.entity.LeaveRequest;
import com.lms.entity.LeaveStatus;
import com.lms.entity.Notification;
import com.lms.repository.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository,
            EmployeeRepository employeeRepository,
            LeaveRequestRepository leaveRepository,
            NotificationRepository notificationRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public LeaveRequest getLeaveById(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found with id: " + id));
    }

    // ================= DASHBOARD =================
    public DashboardData getDashboardData() {

        DashboardData data = new DashboardData();
        LocalDate today = LocalDate.now();

        List<LeaveRequest> leaves = leaveRepository.findAll();

        long totalEmployees = employeeRepository.count();

        List<String> leaveEmails = leaves.stream()
                .filter(l -> l.getStartDate() != null &&
                        l.getEndDate() != null &&
                        !today.isBefore(l.getStartDate()) &&
                        !today.isAfter(l.getEndDate()) &&
                        l.getStatus() == LeaveStatus.APPROVED)
                .map(l -> l.getEmployee().getEmail())
                .distinct()
                .toList();

        long activeEmployees = employeeRepository.countByActiveTrue() - leaveEmails.size();

        data.setTotalEmployees(totalEmployees);
        data.setActiveEmployees(activeEmployees);
        data.setOnLeaveToday(leaveEmails.size());

        data.setTotalLeaves(leaves.size());
        data.setPendingRequests(leaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count());
        data.setApprovedLeaves(leaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count());
        data.setRejectedLeaves(leaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count());

        data.setRecentLeaves(
                leaves.stream()
                        .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                        .limit(5)
                        .toList());

        return data;
    }

    // ================= LEAVES =================
    public List<LeaveRequest> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public LeaveRequest applyLeave(LeaveRequest leave) {

        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new RuntimeException("Invalid date range");
        }

        leave.setStatus(LeaveStatus.PENDING);
        return leaveRepository.save(leave);
    }

    public LeaveRequest approveLeave(Long id) {

        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus(LeaveStatus.APPROVED);

        LeaveRequest updated = leaveRepository.save(leave);

        createNotification(leave, "Your leave has been approved");

        return updated;
    }

    public LeaveRequest rejectLeave(Long id) {

        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus(LeaveStatus.REJECTED);

        LeaveRequest updated = leaveRepository.save(leave);

        createNotification(leave, "Your leave has been rejected");

        return updated;
    }

    private void createNotification(LeaveRequest leave, String message) {

        Notification emp = new Notification();
        emp.setUserEmail(leave.getEmployee().getEmail());
        emp.setMessage(message);
        emp.setRead(false);
        notificationRepository.save(emp);

        Notification admin = new Notification();
        admin.setUserEmail("admin@gmail.com");
        admin.setMessage("Leave update for: " + leave.getEmployee().getEmail());
        admin.setRead(false);
        notificationRepository.save(admin);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByEmail(String email) {
        return notificationRepository.findAllByUserEmail(email);
    }

    public List<Notification> markAllAsRead(String email) {

        List<Notification> list = notificationRepository.findAllByUserEmail(email);

        list.forEach(n -> n.setRead(true));

        return notificationRepository.saveAll(list);
    }

    public long getUnreadCount(String email) {
        return notificationRepository.countByUserEmailAndReadFalse(email);
    }

    public ReportData getReportData() {

        ReportData data = new ReportData();
        List<LeaveRequest> leaves = leaveRepository.findAll();

        long total = leaves.size();
        long approved = leaves.stream().filter(l -> l.getStatus() == LeaveStatus.APPROVED).count();
        long rejected = leaves.stream().filter(l -> l.getStatus() == LeaveStatus.REJECTED).count();
        long pending = leaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();

        data.setTotalUsers(userRepository.count());
        data.setTotalLeaves(total);
        data.setApprovedLeaves(approved);
        data.setRejectedLeaves(rejected);
        data.setPendingLeaves(pending);

        double totalDays = 0;
        for (LeaveRequest l : leaves) {
            if (l.getStartDate() != null && l.getEndDate() != null) {
                totalDays += ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1;
            }
        }

        data.setAvgDuration(total > 0 ? totalDays / total : 0);
        data.setApprovalRate(total > 0 ? (int) ((approved * 100) / total) : 0);
        data.setActiveStaff((int) employeeRepository.countByActiveTrue());

        return data;
    }

    public List<Map<String, Object>> getLeaveEvents() {

        List<LeaveRequest> leaves = leaveRepository.findAll();
        List<Map<String, Object>> events = new ArrayList<>();

        for (LeaveRequest leave : leaves) {

            Map<String, Object> event = new HashMap<>();

            // safe null handling
            if (leave.getStartDate() == null || leave.getEndDate() == null) {
                continue; // skip invalid records
            }

            event.put("title", leave.getEmployee().getEmail());
            event.put("start", leave.getStartDate().toString());
            event.put("end", leave.getEndDate().toString());

            String color = switch (leave.getStatus()) {
                case APPROVED -> "green";
                case PENDING -> "orange";
                case REJECTED -> "red";
                default -> "gray";
            };

            event.put("color", color);

            events.add(event);
        }

        return events;
    }

    public List<Employee> getActiveEmployees() {

        LocalDate today = LocalDate.now();

        List<String> onLeave = leaveRepository.findAll().stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED &&
                        !today.isBefore(l.getStartDate()) &&
                        !today.isAfter(l.getEndDate()))
                .map(l -> l.getEmployee().getEmail())
                .distinct()
                .toList();

        return employeeRepository.findByActiveTrueAndEmailNotIn(onLeave);
    }

    // public byte[] generateReportPdf() {

    // try {
    // ByteArrayOutputStream out = new ByteArrayOutputStream();

    // PdfWriter writer = new PdfWriter(out);
    // PdfDocument pdf = new PdfDocument(writer);
    // Document document = new Document(pdf);

    // document.add(new Paragraph("LMS REPORT"));
    // document.add(new Paragraph("Total Employees: " +
    // employeeRepository.count()));
    // document.add(new Paragraph("Total Leaves: " + leaveRepository.count()));
    // document.add(new Paragraph("Approved Leaves: " +
    // leaveRepository.findAll().stream()
    // .filter(l -> l.getStatus() == LeaveStatus.APPROVED).count()));

    // document.close();

    // return out.toByteArray();

    // } catch (Exception e) {
    // throw new RuntimeException("PDF generation failed", e);
    // }
    // }

    // public Meeting saveMeeting(Meeting meeting) {

    // Meeting saved = meetingRepository.save(meeting);

    // // ✅ EMAIL SEND LOGIC
    // for (String email : meeting.getParticipants()) {

    // emailService.sendEmail(
    // email,
    // "Meeting Invitation 📅",
    // "Hello,\n\nYou are invited to a meeting.\n\n" +
    // "Title: " + meeting.getTitle() +
    // "\nDate: " + meeting.getMeetingDateTime() +
    // "\n\nRegards,\nAdmin");
    // }

    // return saved;
    // }

    @Autowired
    private NotificationRepository notificationRepo;

    public Notification markAsRead(Long id) {
        Notification n = notificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        n.setRead(true);
        return notificationRepo.save(n);
    }

    // ✅ DELETE
    public void deleteNotification(Long id) {
        notificationRepo.deleteById(id);
    }
}
