package com.LeaveManagement.EmployeeLeaveManagement.controller;

import com.LeaveManagement.EmployeeLeaveManagement.dto.AuthResponse;
import com.LeaveManagement.EmployeeLeaveManagement.dto.LoginRequest;
import com.LeaveManagement.EmployeeLeaveManagement.dto.RegisterRequest;
import com.LeaveManagement.EmployeeLeaveManagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/check")
    public String check(){
        return "Working fine";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
