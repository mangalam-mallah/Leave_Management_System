package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.dto.RegisterRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveBalance;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveType;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import com.LeaveManagement.EmployeeLeaveManagement.exception.BadRequestException;
import com.LeaveManagement.EmployeeLeaveManagement.exception.ResourceNotFoundException;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveBalanceRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveTypeRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    // List all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // List user by role
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Create a user by admin
    @Transactional
    public User createUser(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User manager = null;
        if(request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .manager(manager)
                .build();

        User saved = userRepository.save(user);

        // Auto-create leave balances
        if(request.getRole() == Role.EMPLOYEE) {
            initLeaveBalances(saved);
        }

        log.info("Admin created user: {} with role {}", saved.getEmail(), saved.getRole());
        return saved;
    }

    //Assign or change a user's manager
    @Transactional
    public User assignManager(Long employeeId, Long managerId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with id: " + managerId));

        if(manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN){
            throw new BadRequestException("Target user is not manager or admin");
        }

        employee.setManager(manager);
        return userRepository.save(employee);
    }

    // List all leave types
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    // Create a leave type
    public LeaveType createLeaveType(LeaveType leaveType) {
        if(leaveTypeRepository.existsByName(leaveType.getName())) {
            throw new BadRequestException(
                    "Leave type already exists: " + leaveType.getName());
        }

        return leaveTypeRepository.save(leaveType);
    }

    // Update an existing leave type
    @Transactional
    public LeaveType updateLeaveType(Long id, LeaveType updated) {
        LeaveType existing = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave type not found with id: " + id));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setMaxDaysPerYear(updated.getMaxDaysPerYear());

        return leaveTypeRepository.save(existing);
    }

    // Delete a leave type
    public void deleteLeaveType(Long id) {
        if (!leaveTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Leave type not found with id: " + id);
        }
        leaveTypeRepository.deleteById(id);
        log.info("Admin deleted leave type id: {}", id);
    }

    // Init leave balances for a new employee
    // Called after creating a new EMPLOYEE — seeds zero-balance rows
    // for every existing leave type for the current year
    public void initLeaveBalances(User employee) {
        int year = LocalDate.now().getYear();
        List<LeaveType> allTypes = leaveTypeRepository.findAll();

        for(LeaveType type : allTypes) {
            boolean exists = leaveBalanceRepository
                    .findByUserIdAndLeaveTypeIdAndYear(employee.getId(), type.getId(), year)
                    .isPresent();

            if (!exists) {
                leaveBalanceRepository.save(LeaveBalance.builder()
                        .user(employee)
                        .leaveType(type)
                        .year(year)
                        .totalDays(type.getMaxDaysPerYear())
                        .usedDays(0)
                        .build());
            }

            log.info("Initialized {} leave balance records for {}",
                    allTypes.size(), employee.getEmail());
        }
    }

}
