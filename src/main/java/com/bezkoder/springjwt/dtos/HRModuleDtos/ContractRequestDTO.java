package com.bezkoder.springjwt.dtos.HRModuleDtos;
import com.bezkoder.springjwt.models.HRModuleEntities.ContractStatus;
import com.bezkoder.springjwt.models.HRModuleEntities.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Use LocalDate for dates

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequestDTO {
    @NotNull(message = "Le type de contrat est obligatoire")
    private ContractType contractType;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;
    private LocalDate renewalDate;

    @NotBlank(message = "La référence du contrat est obligatoire")
    private String reference;

    private String description;

    @NotNull(message = "Le statut du contrat est obligatoire")
    private ContractStatus statut;

    // *** THIS FIELD MUST MATCH THE FRONTEND PAYLOAD KEY AND BACKEND ENTITY ID TYPE ***
    @NotNull(message = "Employee ID must be provided") // Adding backend validation to DTO
    private Long employeeId; // <-- Matches frontend's 'employeeId' (as number, binds to Long)

    // Note: File content, name, type are NOT in this DTO, as they come from the file part.
    // You only need fields from the JSON part.
}

