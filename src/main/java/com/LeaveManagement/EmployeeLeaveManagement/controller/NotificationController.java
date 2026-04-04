package com.LeaveManagement.EmployeeLeaveManagement.controller;

import com.LeaveManagement.EmployeeLeaveManagement.dto.NotificationRequest;
import com.LeaveManagement.EmployeeLeaveManagement.dto.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> send(@RequestBody NotificationRequest request) {
        System.out.print("Notification controller");
        logger.info("📧 Notification received:");
        logger.info("   To      : {}", request.getEmail());
        logger.info("   Subject : {}", request.getSubject());
        logger.info("   Message : {}", request.getMessage());

        return ResponseEntity.ok(new NotificationResponse(true, "Notification processed Successfully"));
    }

}
