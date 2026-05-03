package com.lms.dto;

public class LeaveDTO {

    private Long id;
    private String employeeName;
    private String reason;
    private String status;

    public LeaveDTO(Long id, String employeeName, String reason, String status) {
        this.id = id;
        this.employeeName = employeeName;
        this.reason = reason;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
}