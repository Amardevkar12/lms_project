package com.lms.dto;

public class ReportData {

    private long totalUsers;
    private long totalLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;
    private long pendingLeaves;

    private double avgDuration;
    private double approvalRate;
    private long activeStaff;
    private long monthlyLeaves;

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getMonthlyLeaves() {
        return monthlyLeaves;
    }

    public long getTotalLeaves() {
        return totalLeaves;
    }

    public long getApprovedLeaves() {
        return approvedLeaves;
    }

    public long getRejectedLeaves() {
        return rejectedLeaves;
    }

    public long getPendingLeaves() {
        return pendingLeaves;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public void setApprovalRate(double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public void setActiveStaff(long activeStaff) {
        this.activeStaff = activeStaff;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public void setTotalLeaves(long totalLeaves) {
        this.totalLeaves = totalLeaves;
    }

    public void setApprovedLeaves(long approvedLeaves) {
        this.approvedLeaves = approvedLeaves;
    }

    public void setRejectedLeaves(long rejectedLeaves) {
        this.rejectedLeaves = rejectedLeaves;
    }

    public void setPendingLeaves(long pendingLeaves) {
        this.pendingLeaves = pendingLeaves;
    }

    public void setMonthlyLeaves(long monthlyLeaves) {
        this.monthlyLeaves = monthlyLeaves;
    }

    public void setAvgDuration(double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public void setApprovalRate(int approvalRate) {
        this.approvalRate = approvalRate;
    }

    public void setActiveStaff(int activeStaff) {
        this.activeStaff = activeStaff;
    }
}