package com.LeaveManagement.EmployeeLeaveManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {
    private String leaveTypeName;
    private String description;
    private int totalDays;
    private int usedDays;
    private int remainingDays;
    private int year;
}
