package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.dto.LeaveRequestResponse;
import com.LeaveManagement.EmployeeLeaveManagement.dto.ReviewRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import com.LeaveManagement.EmployeeLeaveManagement.exception.BadRequestException;
import com.LeaveManagement.EmployeeLeaveManagement.exception.ResourceNotFoundException;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveBalanceRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveRequestRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {
    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private UserRepository userRepository;
    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private LeaveService leaveService;
    @Mock private NotificationService notificationService;
    @Mock private LeaveBalanceService leaveBalanceService;
    @InjectMocks ManagerService managerService;

    private User manager;
    private User employee;
    private LeaveRequest leaveRequest;

    @BeforeEach
    void setUp() {
        manager = User.builder()
                .id(1L)
                .email("manager@test.com")
                .build();

        employee = User.builder()
                .id(2L)
                .manager(manager)
                .build();

        leaveRequest = LeaveRequest.builder()
                .id(100L)
                .employee(employee)
                .leaveStatus(LeaveStatus.PENDING)
                .build();
    }

    @Test
    void shouldApproveLeaveSuccessfully() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.APPROVED);
        request.setComment("Leave Approved");

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any())).thenReturn(leaveRequest);
        when(leaveService.mapToResponse(any())).thenReturn(new LeaveRequestResponse());

        LeaveRequestResponse response =
                managerService.reviewLeave(manager.getEmail(), 100L, request);

        // verify balance deduction
        verify(leaveBalanceService).deductBalance(leaveRequest);

        // verify notification
        verify(notificationService).notifyLeaveApproved(leaveRequest);

        // verify status updated
        assertEquals(LeaveStatus.APPROVED, leaveRequest.getLeaveStatus());
        assertNotNull(response);
    }

    @Test
    void shouldRejectLeaveSuccessfully() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.REJECTED);
        request.setComment("Leave Rejected");

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any())).thenReturn(leaveRequest);
        when(leaveService.mapToResponse(any())).thenReturn(new LeaveRequestResponse());

        managerService.reviewLeave(manager.getEmail(), 100L, request);

        verify(leaveBalanceService, never()).deductBalance(any());
        verify(notificationService).notifyLeaveRejected(leaveRequest);

        assertEquals(LeaveStatus.REJECTED, leaveRequest.getLeaveStatus());
    }

    @Test
    void shouldThrowExceptionForInvalidStatus() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.PENDING);

        assertThrows(BadRequestException.class, () ->
                managerService.reviewLeave(manager.getEmail(), 100L, request));
    }

    @Test
    void shouldThrowExceptionWhenLeaveNotFound() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.APPROVED);

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                managerService.reviewLeave(manager.getEmail(), 100L, request));
    }

    @Test
    void shouldThrowExceptionWhenNotManagerEmployee() {
        User anotherManager = User.builder().id(99L).build();
        employee.setManager(anotherManager);

        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.APPROVED);

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));

        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

        assertThrows(BadRequestException.class, () ->
                managerService.reviewLeave(manager.getEmail(), 100L, request));
    }

    @Test
    void shouldThrowExceptionWhenAlreadyReviewed() {
        leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);

        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.REJECTED);

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));

        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

        assertThrows(BadRequestException.class, () ->
                managerService.reviewLeave(manager.getEmail(), 100L, request));
    }

    @Test
    void shouldThrowExceptionWhenBalanceInsufficient() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus(LeaveStatus.APPROVED);

        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));

        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

        doThrow(new BadRequestException("Insufficient balance")).when(leaveBalanceService).deductBalance(any());

        assertThrows(BadRequestException.class, () ->
                managerService.reviewLeave(manager.getEmail(), 100L, request));

        verify(notificationService, never()).notifyLeaveApproved(any());
    }

}
