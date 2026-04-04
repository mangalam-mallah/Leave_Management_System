package com.LeaveManagement.EmployeeLeaveManagement.dto;

import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Leave Request Response (outgoing to client)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {
    private Long id;
    private String employeeName;
    private String employeeEmail;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private long numberOfDays;
    private String reason;
    private LeaveStatus leaveStatus;
    private String managerComment;
    private String reviewedByName;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;

}
