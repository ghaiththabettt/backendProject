package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

@Data
public class PolicyDTO {
    private Long policyId;
    private String policyName;
    private String description;

    public Long getDepartmentId() {
        return null;
    }
}
