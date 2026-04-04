package com.LeaveManagement.EmployeeLeaveManagement.service;

import com.LeaveManagement.EmployeeLeaveManagement.entity.LeaveRequest;
import com.LeaveManagement.EmployeeLeaveManagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;

    public void notifyLeaveApplied(LeaveRequest request) {
        String email = request.getEmployee().getEmail();
        String subject = "Leave Application Applied";
        String message = String.format(
                "Hi %s,\n\n" +
                        "Your %s leave request from %s to %s has been Applied.\n\n",
                request.getEmployee().getFullName(),
                request.getLeaveType().getName(),
                request.getStartDate(),
                request.getEndDate()
        );
        sendEmail(email, subject, message, null);
    }

    public void notifyLeaveApproved(LeaveRequest request) {
        User manager = request.getReviewedBy();
        String email = request.getEmployee().getEmail();
        String subject = "Leave Application Approved";
        String message = String.format(
                "Hi %s,\n\n" +
                        "Your %s leave request from %s to %s has been APPROVED.\n\n" +
                        "Approved by: %s (%s)\n\n" +
                        "Regards,\nLeave Management System",
                request.getEmployee().getFullName(),
                request.getLeaveType().getName(),
                request.getStartDate(),
                request.getEndDate(),
                manager.getFullName(),
                manager.getEmail()
        );
        sendEmail(email, subject, message, manager.getEmail());
    }

    public void notifyLeaveRejected(LeaveRequest request) {
        User manager = request.getReviewedBy();
        String email = request.getEmployee().getEmail();
        String subject = "Leave Application Rejected";
        String message = String.format(
                "Hi %s,\n\n" +
                        "Your %s leave request from %s to %s has been REJECTED.\n\n" +
                        "Approved by: %s (%s)\n\n" +
                        "Regards,\nLeave Management System",
                request.getEmployee().getFullName(),
                request.getLeaveType().getName(),
                request.getStartDate(),
                request.getEndDate(),
                manager.getFullName(),
                manager.getEmail()
        );
        sendEmail(email, subject, message, manager.getEmail());
    }

    public void notifyLeaveCancelled(LeaveRequest request) {
        User manager = request.getReviewedBy();
        String email = request.getEmployee().getEmail();
        String subject = "Leave Request Cancelled";
        String message = String.format(
                "Hi %s,\n\n" +
                        "Your %s leave request from %s to %s has been CANCELLED.\n\n" +
                        "Approved by: %s (%s)\n\n" +
                        "Regards,\nLeave Management System",
                request.getEmployee().getFullName(),
                request.getLeaveType().getName(),
                request.getStartDate(),
                request.getEndDate(),
                manager.getFullName(),
                manager.getEmail()
        );
        sendEmail(email, subject, message, manager.getEmail());
    }

    private void sendEmail(String email, String subject, String message, String managerEmail) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("noreply@company.com");
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            if(managerEmail != null) {
                mailMessage.setReplyTo(managerEmail);
            }
            mailSender.send(mailMessage);
            logger.info("Email successfully sent to {}", email);
        } catch (Exception ex) {
            logger.error("Failed to send email to {}: {}", email, message);
        }
    }
}
