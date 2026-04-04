package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveApplyRequest;
import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveRequestResponse;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveBalance;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveType;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import com.LeaveManagement.EmployeeLeaveManagement.exception.BadRequestException;
import com.LeaveManagement.EmployeeLeaveManagement.exception.ResourceNotFoundException;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveBalanceRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveRequestRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveTypeRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Apply for leave
    @Transactional
    public LeaveRequestResponse applyLeave(String employeeEmail, LeaveApplyRequest request) {

        // 1. Load Employee
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // 2. Load leave type
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + request.getLeaveTypeId()));

        // 3. Validate date range
        if (!request.getEndDate().isAfter(request.getStartDate()) &&
            !request.getEndDate().isEqual(request.getStartDate())) {
            throw new BadRequestException("End date must be on or after the start date");
        }

        // 4. Calculate number of days requested
        long daysRequested = request.getStartDate()
                .datesUntil(request.getEndDate().plusDays(1))
                .count();

        // 5. Check for overlapping leave request
        List<LeaveRequest> overlaps = leaveRequestRepository.findOverlappingRequests(
                employee.getId(), request.getStartDate(), request.getEndDate());
        if(!overlaps.isEmpty()) {
            throw new BadRequestException(
                    "You already have a leave request overlapping these dates"
            );
        }

        // 6.Check leave balance
        int currentYear = LocalDate.now().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByUserIdAndLeaveTypeIdAndYear(
                employee.getId(), leaveType.getId(), currentYear)
                .orElseThrow(() -> new BadRequestException(
                "No leave balance found for " + leaveType.getName() + " in year " + currentYear
        ));

        if(balance.getRemainingDays() < daysRequested){
            throw new BadRequestException("Insufficient leave balance. Requested: " + daysRequested +
                    " days, Available: " + balance.getRemainingDays() + " days");
        }

        // 7. Save the leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .leaveStatus(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request #{} applied by {} for {} days ({} to {})",
                saved.getId(), employeeEmail, daysRequested,
                request.getStartDate(), request.getEndDate());

        notificationService.notifyLeaveApplied(saved);
        return mapToResponse(saved);
    }

    // View own requests
    public List<LeaveRequestResponse> getMyRequests(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return leaveRequestRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // View own request filtered by status
    public List<LeaveRequestResponse> getMyRequestByStatus(String employeeEmail, LeaveStatus leaveStatus) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return leaveRequestRepository.findByEmployeeIdAndLeaveStatus(employee.getId(), leaveStatus)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // View Leave Balance
    public List<LeaveBalance> getMyBalance(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        int currentYear = LocalDate.now().getYear();

        return leaveBalanceRepository.findByUserIdAndYear(employee.getId(), currentYear);
    }

    // Cancel a pending request
    @Transactional
    public LeaveRequestResponse cancelLeave(String employeeEmail, Long leaveRequestId) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request with id: " + leaveRequestId + " not found"
                ));

        if(!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new BadRequestException("You can only cancel your own requests");
        }

        if(leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending request can be cancelled. Current status: "
                    + leaveRequest.getLeaveStatus());
        }

        leaveRequest.setLeaveStatus(LeaveStatus.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        notificationService.notifyLeaveCancelled(leaveRequest);

        log.info("Leave request #{} cancelled by {}", leaveRequestId, employeeEmail);

        return mapToResponse(updated);
    }

    // Get single request by id
    public LeaveRequestResponse getRequestById(String employeeEmail, long leaveRequestId) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found with id: " + leaveRequestId));

        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new BadRequestException("Access denied: not your leave request");
        }

        return mapToResponse(leaveRequest);
    }

    // Entity to Response Dto
    LeaveRequestResponse mapToResponse(LeaveRequest lr) {
        return LeaveRequestResponse.builder()
                .id(lr.getId())
                .employeeName(lr.getEmployee().getFullName())
                .employeeEmail(lr.getEmployee().getEmail())
                .leaveTypeName(lr.getLeaveType().getName())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .numberOfDays(lr.getNumberOfDays())
                .reason(lr.getReason())
                .leaveStatus(lr.getLeaveStatus())
                .managerComment(lr.getManagerComment())
                .reviewedByName(lr.getReviewedBy() != null
                        ? lr.getReviewedBy().getFullName() : null)
                .appliedAt(lr.getAppliedAt())
                .reviewedAt(lr.getReviewedAt())
                .build();
    }

}
