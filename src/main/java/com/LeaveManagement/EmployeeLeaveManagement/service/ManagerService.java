package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveRequestResponse;
import com.LeaveManagement.EmployeeLeaveManagement.dto.ReviewRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import com.LeaveManagement.EmployeeLeaveManagement.exception.BadRequestException;
import com.LeaveManagement.EmployeeLeaveManagement.exception.ResourceNotFoundException;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveRequestRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ManagerService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveService leaveService;
    private final NotificationService notificationService;

    // View all requests from the team
    public List<LeaveRequestResponse> getTeamRequests(String managerEmail) {
        User manager = getManagerByEmail(managerEmail);
        return leaveRequestRepository.findByManagerId(manager.getId())
                .stream()
                .map(leaveService::mapToResponse)
                .toList();
    }

    // View team request filtered by status
    public List<LeaveRequestResponse> getTeamRequestsByStatus(
            String managerEmail, LeaveStatus status
    ) {
        User manager = getManagerByEmail(managerEmail);
        return leaveRequestRepository.findByManagerIdAndLeaveStatus(manager.getId(), status)
                .stream()
                .map(leaveService::mapToResponse)
                .toList();
     }

    // Approve or Reject a leave request
     public LeaveRequestResponse reviewLeave(
             String managerEmail, Long leaveRequestId, ReviewRequest reviewRequest
     ) {
         // 1. Validate action — only APPROVED or REJECTED allowed
         if (reviewRequest.getStatus() != LeaveStatus.APPROVED &&
                 reviewRequest.getStatus() != LeaveStatus.REJECTED) {
             throw new BadRequestException(
                     "Review status must be APPROVED or REJECTED");
         }

         // 2. Load manager
         User manager = getManagerByEmail(managerEmail);

         // 3. Load leave request
         LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                 .orElseThrow(() -> new ResourceNotFoundException(
                         "Leave request not found with id: " + leaveRequestId));

         // 4. Verify this request belongs to manager's team
         User employee = leaveRequest.getEmployee();
         if (employee.getManager() == null ||
                 !employee.getManager().getId().equals(manager.getId())) {
             throw new BadRequestException(
                     "This leave request does not belong to your team");
         }

         // 5. Only PENDING requests can be reviewed
         if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
             throw new BadRequestException(
                     "Only PENDING requests can be reviewed. Current status: "
                             + leaveRequest.getLeaveStatus());
         }

         // 6. If approving — deduct balance first (throws if insufficient)
         if (reviewRequest.getStatus() == LeaveStatus.APPROVED) {
             leaveBalanceService.deductBalance(leaveRequest);
         }

         // 7. Update the leave request
         leaveRequest.setLeaveStatus(reviewRequest.getStatus());
         leaveRequest.setManagerComment(reviewRequest.getComment());
         leaveRequest.setReviewedBy(manager);
         leaveRequest.setReviewedAt(LocalDateTime.now());

         LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
         if(updated.getLeaveStatus() == LeaveStatus.APPROVED) {
             notificationService.notifyLeaveApproved(updated);
         } else if(updated.getLeaveStatus() == LeaveStatus.REJECTED) {
             notificationService.notifyLeaveRejected(updated);
         }

         log.info("Leave request #{} {} by manager {}",
                 leaveRequestId, reviewRequest.getStatus(), managerEmail);

         return leaveService.mapToResponse(updated);
     }

    // Helper
    private User getManagerByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with email " + email
                ));
    }
}
