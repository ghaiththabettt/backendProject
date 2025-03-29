package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private Long expenseId;
    private String type;   // "Meal", "Transport", "Hotel", etc.
    private Double amount;
    private LocalDate date;
    private String status; // "Pending", "Approved", "Rejected"

    private Long employeeId;
}
