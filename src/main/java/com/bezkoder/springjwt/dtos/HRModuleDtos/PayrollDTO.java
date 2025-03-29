package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PayrollDTO {
    private Long payrollId;
    private Double baseSalary;
    private Double deductions;
    private Double bonuses;
    private LocalDate payDate;

    private Long employeeId;
}
