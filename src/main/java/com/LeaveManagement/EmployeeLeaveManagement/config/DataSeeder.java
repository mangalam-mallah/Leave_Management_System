package com.LeaveManagement.EmployeeLeaveManagement.config;

import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveBalance;
import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveType;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveBalanceRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.LeaveTypeRepository;
import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if(userRepository.count() > 0) {
                log.info("Seed Data already present");
                return;
            }

            int currentYear = LocalDateTime.now().getYear();

            LeaveType sick = leaveTypeRepository.save(
                    LeaveType.builder()
                            .name("Sick leave")
                            .description("Leave due to illness or medical reasons")
                            .maxDaysPerYear(12)
                            .build()
            );

            LeaveType casual = leaveTypeRepository.save(LeaveType.builder()
                    .name("Casual Leave")
                    .description("Leave for personal or family matters")
                    .maxDaysPerYear(10)
                    .build());

            LeaveType earned = leaveTypeRepository.save(LeaveType.builder()
                    .name("Earned Leave")
                    .description("Leave earned through service")
                    .maxDaysPerYear(20)
                    .build());

            log.info("Seeded 3 leave types.");

            User admin = userRepository.save(
                    User.builder()
                            .fullName("Admin User")
                            .email("admin@company.com")
                            .password(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .build()
            );

            User manager = userRepository.save(User.builder()
                    .fullName("Rajesh Sharma")
                    .email("manager@company.com")
                    .password(passwordEncoder.encode("manager123"))
                    .role(Role.MANAGER)
                    .build());

            // ── 4. Employee ───────────────────────────────────
            User employee = userRepository.save(User.builder()
                    .fullName("Priya Patel")
                    .email("employee@company.com")
                    .password(passwordEncoder.encode("employee123"))
                    .role(Role.EMPLOYEE)
                    .manager(manager)
                    .build());

            log.info("Seeded admin, manager, employee.");

            List.of(
                    LeaveBalance.builder().user(employee).leaveType(sick)
                            .year(currentYear).totalDays(12).usedDays(0).build(),
                    LeaveBalance.builder().user(employee).leaveType(casual)
                            .year(currentYear).totalDays(10).usedDays(0).build(),
                    LeaveBalance.builder().user(employee).leaveType(earned)
                            .year(currentYear).totalDays(20).usedDays(0).build()
            ).forEach(leaveBalanceRepository::save);

        };

    }
}
