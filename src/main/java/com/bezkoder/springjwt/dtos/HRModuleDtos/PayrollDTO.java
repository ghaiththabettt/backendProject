package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PayrollDTO {
    private Long payrollId;
    private Double basicSalary;
    private Double bonuses;
    private Double deductions;
    private Double totalSalary;
    private LocalDate payDate;

    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String employeeDepartment;
}
