package com.LeaveManagement.EmployeeLeaveManagement.repository;

import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Find all employees under a specific manager
    List<User> findByManagerId(Long managerId);

    // Find all user by role(used by Admin)
    List<User> findByRole(Role role);
}
