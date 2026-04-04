package com.LeaveManagement.EmployeeLeaveManagement.controller;

import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveRequestResponse;
import com.LeaveManagement.EmployeeLeaveManagement.dto.ReviewRequest;
import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import com.LeaveManagement.EmployeeLeaveManagement.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService managerService;

    // View all leave requests from the manager's team.
    @GetMapping("/team/leaves")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> getTeamLeaves(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) LeaveStatus status
            ) {
                  List<LeaveRequestResponse> response = (status != null) ?
                          managerService.getTeamRequestsByStatus(userDetails.getPassword(), status) :
                          managerService.getTeamRequests(userDetails.getUsername());

                  return ResponseEntity.ok(response);
            }

     // Approve or reject a leave request.
    @PutMapping("/leaves/{id}/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestResponse> reviewLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest
            ) {
                    System.out.println("Here "+userDetails);
                LeaveRequestResponse response = managerService
                        .reviewLeave(userDetails.getUsername(), id, reviewRequest);

                return ResponseEntity.ok(response);
            }

}
