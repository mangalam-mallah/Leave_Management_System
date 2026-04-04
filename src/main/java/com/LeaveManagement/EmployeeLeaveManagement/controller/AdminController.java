package com.LeaveManagement.EmployeeLeaveManagement.controller;

import com.LeaveManagement.EmployeeLeaveManagement.dto.RegisterRequest;
import com.LeaveManagement.EmployeeLeaveManagement.dto.UserResponse;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveType;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import com.LeaveManagement.EmployeeLeaveManagement.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // Get all users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam (required = false) Role role
            ) {
        List<User> users = (role != null) ?
                adminService.getUsersByRole(role) :
                adminService.getAllUsers();

        return ResponseEntity.ok(users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList()));
    }

    // Create User
    @PostMapping("/createUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody RegisterRequest request
            ) {
        User user = adminService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToUserResponse(user));
    }

    // Assign manager to emp
    @PatchMapping("/users/{employeeId}/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignManager(
            @PathVariable Long employeeId,
            @PathVariable Long managerId
    ) {
        User updated = adminService.assignManager(employeeId, managerId);
        return ResponseEntity.ok(mapToUserResponse(updated));
    }

    // Get leave type
    @GetMapping("/leaves-type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(adminService.getAllLeaveTypes());
    }

    // Post leave type
    @PostMapping("/leaves-type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveType> createLeaveType(
            @RequestBody LeaveType leaveType
    ) {
        return ResponseEntity.ok(adminService.createLeaveType(leaveType));
    }

    // Edit leave type
    @PatchMapping("/leaves-type/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveType> updateLeaveType (
            @PathVariable Long id,
            @RequestBody LeaveType leaveType
    ) {
        return ResponseEntity.ok(adminService.updateLeaveType(id, leaveType));
    }

    // Delete leave type
    @DeleteMapping("/leaves-type/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Long id) {
        adminService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .managerName(user.getManager() != null
                        ? user.getManager().getFullName() : null)
                .managerEmail(user.getManager() != null
                        ? user.getManager().getEmail() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
