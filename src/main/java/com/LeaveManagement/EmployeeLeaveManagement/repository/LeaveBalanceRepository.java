package com.LeaveManagement.EmployeeLeaveManagement.repository;

import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    // Used when Employee checks their balance for a specific leave type
    Optional<LeaveBalance> findByUserIdAndLeaveTypeIdAndYear(Long userId, Long leaveTypeId, Integer year);

    // Used to show all balances for an employee in a given year
    List<LeaveBalance> findByUserIdAndYear(Long userId, Integer year);
}
