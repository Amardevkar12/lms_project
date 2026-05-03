package com.lms.dto;

public class DashboardDTO {

    private long totalUsers;
    private long activeUsers;

    private long totalLeaves;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;

    

    public long getTotalUsers() { return totalUsers; }
    public long getActiveUsers() { return activeUsers; }

    public long getTotalLeaves() { return totalLeaves; }
    public long getPendingLeaves() { return pendingLeaves; }
    public long getApprovedLeaves() { return approvedLeaves; }
    public long getRejectedLeaves() { return rejectedLeaves; }

    
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public void setTotalLeaves(long totalLeaves) { this.totalLeaves = totalLeaves; }
    public void setPendingLeaves(long pendingLeaves) { this.pendingLeaves = pendingLeaves; }
    public void setApprovedLeaves(long approvedLeaves) { this.approvedLeaves = approvedLeaves; }
    public void setRejectedLeaves(long rejectedLeaves) { this.rejectedLeaves = rejectedLeaves; }
}