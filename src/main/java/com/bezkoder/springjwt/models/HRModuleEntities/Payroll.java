package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "payrolls")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Relation avec Employee

    private float basicSalary;
    private float bonuses;
    private float deductions;
    private float totalSalary;
    private LocalDate payDate;
}
