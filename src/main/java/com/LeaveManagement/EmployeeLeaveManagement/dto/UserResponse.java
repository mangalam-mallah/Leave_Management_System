package com.LeaveManagement.EmployeeLeaveManagement.dto;

import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String managerName;
    private String managerEmail;
    private LocalDateTime createdAt;
}
