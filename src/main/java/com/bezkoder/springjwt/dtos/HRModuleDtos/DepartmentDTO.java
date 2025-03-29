package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class DepartmentDTO {
    private Long departmentId;
    private String departmentName;

    // Liste d’employés (IDs)
    private List<Long> employeeIds;

    // Liste de postes (IDs)
    private List<Long> jobPositionIds;

    // Liste de formations (IDs)
    private List<Long> trainingIds;

    // Liste de policies (IDs)
    private List<Long> policyIds;
}