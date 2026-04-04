package com.LeaveManagement.EmployeeLeaveManagement.controller;

import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveApplyRequest;
import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveBalanceResponse;
import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveRequestResponse;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveBalance;
import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import com.LeaveManagement.EmployeeLeaveManagement.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class LeaveController {
    private final LeaveService leaveService;

    @PostMapping("/leaves")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestResponse> applyLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LeaveApplyRequest request
            ) {
        LeaveRequestResponse response = leaveService.applyLeave(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/leaves")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> getMyLeaves(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false)LeaveStatus status
            ) {
        List<LeaveRequestResponse> response = (status != null) ?
                leaveService.getMyRequestByStatus(userDetails.getUsername(), status) :
                leaveService.getMyRequests(userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaves/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestResponse> getRequestById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        LeaveRequestResponse response = leaveService.getRequestById(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/leaves/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestResponse> cancelLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        LeaveRequestResponse response = leaveService.cancelLeave(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<LeaveBalanceResponse>> getMyBalance(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<LeaveBalance> balances = leaveService.getMyBalance(userDetails.getUsername());

        List<LeaveBalanceResponse> response = balances.stream()
                .map(b -> LeaveBalanceResponse.builder()
                        .leaveTypeName(b.getLeaveType().getName())
                        .description(b.getLeaveType().getDescription())
                        .totalDays(b.getTotalDays())
                        .usedDays(b.getUsedDays())
                        .remainingDays(b.getRemainingDays())
                        .year(b.getYear())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

}
