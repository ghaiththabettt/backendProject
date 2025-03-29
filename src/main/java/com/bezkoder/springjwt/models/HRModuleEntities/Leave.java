package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter

@NoArgsConstructor
@Table(name = "leaves")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;   // "Annual", "Sick", etc.
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private StatusLeave statusLeave;      // "Pending", "Approved", "Rejected"

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Leave(Long leaveId, LeaveType leaveType, LocalDate startDate, LocalDate endDate, StatusLeave statusLeave, Employee employee) {
    }
}