package com.LeaveManagement.EmployeeLeaveManagement.dto;

import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Response (returned on login/register)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String fullName;
    private String email;
    private Role role;
}
