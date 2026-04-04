package com.LeaveManagement.EmployeeLeaveManagement.dto;

import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Register request
@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    //If role is employee link to their manager
    private Long managerId;
}
