package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.PerksType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PerksDTO {
    private Long perksId;
    private Long employeeId;      // pour faire le lien
    private PerksType perksType;  // AWARD, BENEFITS, etc.
    private LocalDate datePerks;
    private String reason;
}
