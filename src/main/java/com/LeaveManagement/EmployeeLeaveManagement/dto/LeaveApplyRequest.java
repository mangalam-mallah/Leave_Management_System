package com.LeaveManagement.EmployeeLeaveManagement.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

// Apply leave (from Employee)
@Data
public class LeaveApplyRequest {

    @NotNull(message = "Leave Type is required")
    private Long leaveTypeId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String reason;
}
