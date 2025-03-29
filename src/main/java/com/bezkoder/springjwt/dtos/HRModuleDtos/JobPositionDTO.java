package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.JobPosition;
import lombok.Data;

import java.util.List;

@Data
public class JobPositionDTO extends JobPosition {
    private Long jobPositionId;
    private String title;
    private String jobLevel;
    private String description;

    // Optionnel : liste d'employés occupant ce poste
    private List<Long> employeeIds;
}
