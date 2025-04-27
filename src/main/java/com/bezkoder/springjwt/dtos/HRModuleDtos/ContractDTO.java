package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor // Added
@AllArgsConstructor // Added
public class ContractDTO {
    private Long contractId;
    private String contractType; // Use String for DTO
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate renewalDate;
    private String reference;
    private String description; // Added
    private String statut; // Use String for DTO

    // Add Employee information for association (creation/update) and display (read)
    private Long employeeId; // Added
    private String employeeFullName; // Added (for display)
    // Add other employee fields if needed for display, e.g., employeeEmail
}
