package com.LeaveManagement.EmployeeLeaveManagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leave_balance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "leave_type_id", "year"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer totalDays; // allocated for a year

    @Column(nullable = false)
    private Integer usedDays;   // deducted on APPROVED leaves

    // Computed: totalDays - usedDays (not stored, calculated at service layer)
    @Transient
    public int getRemainingDays() {
        return totalDays - usedDays;
    }
}
