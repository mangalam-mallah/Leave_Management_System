package com.LeaveManagement.EmployeeLeaveManagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leave_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;    // e.g. "Sick Leave", "Casual Leave", "Earned Leave"

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer maxDaysPerYear;
}
