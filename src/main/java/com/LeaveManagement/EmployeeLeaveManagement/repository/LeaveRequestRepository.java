package com.LeaveManagement.EmployeeLeaveManagement.repository;

import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveRequest;
import com.LeaveManagement.EmployeeLeaveManagement.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    // Employee: view their own requests
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    // Employee: view requests filtered by status
    List<LeaveRequest> findByEmployeeIdAndLeaveStatus(Long employeeId, LeaveStatus leaveStatus);

    // Manager: view all PENDING requests from their team
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId AND lr.leaveStatus = :status")
    List<LeaveRequest> findByManagerIdAndLeaveStatus(
            @Param("managerId") Long managerId,
            @Param("status") LeaveStatus leaveStatus);

    // Manager: view all requests from their team regardless of status
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId")
    List<LeaveRequest> findByManagerId(@Param("managerId") Long managerId);

    // Check for overlapping leave requests (to prevent duplicate applications)
    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.leaveStatus != 'REJECTED'
          AND lr.leaveStatus != 'CANCELLED'
          AND lr.startDate <= :endDate
          AND lr.endDate   >= :startDate
    """)
    List<LeaveRequest> findOverlappingRequests(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")    LocalDate endDate);
}
