package com.lms.dto;

import java.util.List;
import com.lms.entity.LeaveRequest; // ✅ FIX

public class DashboardData {

    private long totalEmployees;
    private long activeEmployees;

    private long totalLeaves;
    private long pendingRequests;
    private long approvedLeaves;
    private long rejectedLeaves;
    private long onLeaveToday;

    private List<LeaveRequest> recentLeaves; // ✅ FIX

    public DashboardData() {}

    public DashboardData(long totalEmployees, long activeEmployees,
            long totalLeaves, long pendingRequests,
            long approvedLeaves, long rejectedLeaves,
            long onLeaveToday, List<LeaveRequest> recentLeaves) {

        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.totalLeaves = totalLeaves;
        this.pendingRequests = pendingRequests;
        this.approvedLeaves = approvedLeaves;
        this.rejectedLeaves = rejectedLeaves;
        this.onLeaveToday = onLeaveToday;
        this.recentLeaves = recentLeaves;
    }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

    public long getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(long activeEmployees) { this.activeEmployees = activeEmployees; }

    public long getTotalLeaves() { return totalLeaves; }
    public void setTotalLeaves(long totalLeaves) { this.totalLeaves = totalLeaves; }

    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }

    public long getApprovedLeaves() { return approvedLeaves; }
    public void setApprovedLeaves(long approvedLeaves) { this.approvedLeaves = approvedLeaves; }

    public long getRejectedLeaves() { return rejectedLeaves; }
    public void setRejectedLeaves(long rejectedLeaves) { this.rejectedLeaves = rejectedLeaves; }

    public long getOnLeaveToday() { return onLeaveToday; }
    public void setOnLeaveToday(long onLeaveToday) { this.onLeaveToday = onLeaveToday; }

    public List<LeaveRequest> getRecentLeaves() { return recentLeaves; } // ✅ FIX
    public void setRecentLeaves(List<LeaveRequest> recentLeaves) { this.recentLeaves = recentLeaves; } // ✅ FIX
}