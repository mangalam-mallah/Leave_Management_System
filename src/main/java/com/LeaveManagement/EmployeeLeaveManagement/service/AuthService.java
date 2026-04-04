package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.dto.AuthResponse;
import com.LeaveManagement.EmployeeLeaveManagement.dto.LoginRequest;
import com.LeaveManagement.EmployeeLeaveManagement.dto.RegisterRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import com.LeaveManagement.EmployeeLeaveManagement.enums.Role;
import com.LeaveManagement.EmployeeLeaveManagement.exception.BadRequestException;
import com.LeaveManagement.EmployeeLeaveManagement.exception.ResourceNotFoundException;
import com.LeaveManagement.EmployeeLeaveManagement.repository.UserRepository;
import com.LeaveManagement.EmployeeLeaveManagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AdminService adminService;

    // Register
    public AuthResponse register(RegisterRequest request) {
        // 1. Check for duplicate email
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // 2. Resolve Manager if provided
        User manager = null;
        if(request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id: " + request.getManagerId()
                    ));
        }

        // 3. Build and save user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .manager(manager)
                .build();

        userRepository.save(user);
        if (user.getRole() == Role.EMPLOYEE) {
            adminService.initLeaveBalances(user);
        }

        log.info("User registered successfully");

        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(token, user);
    }

    // Login
    public AuthResponse login(LoginRequest request) {

        // 1. Delegate credential verification to Spring Security's AuthenticationManager
        //    This internally calls UserDetailsService.loadUserByUsername()
        //    and BCryptPasswordEncoder.matches() — throws AuthenticationException on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If we reach here, credentials are valid — load user and issue token
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user);
        log.info("User logged in successfully");
        log.info("User ID: {} ", user.getId());
        log.info("User ID: {} ", user.getFullName());
        return buildAuthResponse(token, user);
    }


    // Helper
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
