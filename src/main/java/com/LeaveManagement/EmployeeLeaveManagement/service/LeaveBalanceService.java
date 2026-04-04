package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveRequest;
import com.LeaveManagement.EmployeeLeaveManagement.exception.BadRequestException;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveBalanceRepository;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveBalance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceService {
    private final   LeaveBalanceRepository leaveBalanceRepository;

    // Called by ManagerService when APPROVING a leave request
    @Transactional
    public void deductBalance(LeaveRequest leaveRequest) {
        int year = leaveRequest.getStartDate().getYear();
        long days = leaveRequest.getNumberOfDays();

        LeaveBalance leaveBalance = leaveBalanceRepository
                .findByUserIdAndLeaveTypeIdAndYear(
                        leaveRequest.getEmployee().getId(),
                        leaveRequest.getLeaveType().getId(),
                        year
                ).orElseThrow(
                        () -> new BadRequestException(
                                "No balance record found to deduct from")
                );

        if(leaveBalance.getRemainingDays() < days) {
            throw new BadRequestException(
                    "Insufficient balance at time of approval. Available: "
                            + leaveBalance.getRemainingDays() + ", Requested: " + days);
        }

        leaveBalance.setUsedDays(leaveBalance.getUsedDays() + (int) days);
        leaveBalanceRepository.saveAndFlush(leaveBalance);

        log.info("Deducted {} days from {}'s {} balance. Remaining: {}",
                days,
                leaveRequest.getEmployee().getEmail(),
                leaveRequest.getLeaveType().getName(),
                leaveBalance.getRemainingDays());
    }

    // Called by ManagerService when REJECTING — restores balance if already deducted
    @Transactional
    public void restoreBalance(LeaveRequest leaveRequest) {
        int year = leaveRequest.getStartDate().getYear();
        long days = leaveRequest.getNumberOfDays();

        leaveBalanceRepository
                .findByUserIdAndLeaveTypeIdAndYear(
                        leaveRequest.getEmployee().getId(),
                        leaveRequest.getLeaveType().getId(),
                        year)
                .ifPresent(balance -> {
                    int restored = Math.max(0, balance.getUsedDays() - (int) days);
                    balance.setUsedDays(restored);
                    leaveBalanceRepository.save(balance);
                    log.info("Restored {} days to {}'s {} balance",
                            days,
                            leaveRequest.getEmployee().getEmail(),
                            leaveRequest.getLeaveType().getName());
                });
    }
}
