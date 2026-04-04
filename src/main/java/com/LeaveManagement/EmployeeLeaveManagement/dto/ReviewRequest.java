package com.LeaveManagement.EmployeeLeaveManagement.dto;

import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Manager reviews a leave request
@Data
public class ReviewRequest {
    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private LeaveStatus status;
    private String comment;
}
