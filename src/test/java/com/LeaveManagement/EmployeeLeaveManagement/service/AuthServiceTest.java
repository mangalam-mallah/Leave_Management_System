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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("123456");
        registerRequest.setRole(Role.EMPLOYEE);
        registerRequest.setManagerId(null);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("123456");

        savedUser = User.builder()
                .fullName("John Doe")
                .email("test@gmail.com")
                .password("123456")
                .role(Role.EMPLOYEE)
                .build();
    }

    @Test
    void registerSuccess() {
        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("test@gmail.com");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);

        verify(userRepository).existsByEmail("test@gmail.com");
        verify(passwordEncoder).encode("123456");
        verify(jwtUtil).generateToken(any(User.class));
        verify(userRepository).save(any(User.class));
    }

//    @Test
//    void registerWithDupEmail() {
//        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);
//        assertThatThrownBy(() -> authService.register(registerRequest))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("Email already exists");
//
//        verify(userRepository, never()).save(any());
//        verify(jwtUtil, never()).generateToken(any());
//    }
//
//    @Test
//    void registerWithManagerId_ResolvesManagerSuccessfully() {
//        registerRequest.setManagerId(5L);
//
//        User manager = User.builder()
//                .fullName("Manager Mike")
//                .email("mike@company.com")
//                .role(Role.MANAGER)
//                .build();
//
//        when(userRepository.existsByEmail("john@company.com")).thenReturn(false);
//        when(userRepository.findById(5L)).thenReturn(Optional.of(manager));
//        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock-jwt-token");
//
//        AuthResponse response = authService.register(registerRequest);
//
//        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
//        verify(userRepository).findById(5L); // manager was looked up
//    }
//
//    @Test
//    void registerWithInvalidManagerId_ThrowsResourceNotFoundException() {
//        // Arrange
//        registerRequest.setManagerId(99L); // non-existent manager
//
//        when(userRepository.existsByEmail("john@company.com")).thenReturn(false);
//        when(userRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> authService.register(registerRequest))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("Manager not found with id: 99");
//
//        verify(userRepository, never()).save(any());
//    }

    @Test
    void loginSuccess() {
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("test@gmail.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@gmail.com");
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsBadCredentialsException() {
        // Arrange — simulate Spring Security rejecting the credentials
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        // User was never looked up — failed at authentication stage
        verify(userRepository, never()).findByEmail(any());
        verify(jwtUtil, never()).generateToken(any());
    }

//    @Test
//    void login_UserNotFoundAfterAuth_ThrowsResourceNotFoundException() {
//        // Arrange — auth passes but user somehow missing from DB (edge case)
//        when(authenticationManager.authenticate(any())).thenReturn(null);
//        when(userRepository.findByEmail("john@company.com")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> authService.login(loginRequest))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("User not found");
//
//        verify(jwtUtil, never()).generateToken(any());
//    }

}
