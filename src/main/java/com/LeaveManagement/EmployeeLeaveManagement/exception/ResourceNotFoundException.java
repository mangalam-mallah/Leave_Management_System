package com.LeaveManagement.EmployeeLeaveManagement.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
