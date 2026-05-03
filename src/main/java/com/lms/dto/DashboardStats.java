package com.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {
    // Employee stats
    private int totalLeaves;
    private int usedLeaves;
    private int remainingLeaves;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;

    // Manager stats
    private long totalRequests;
    private long totalPending;
    private long totalApproved;
    private long totalRejected;
}
