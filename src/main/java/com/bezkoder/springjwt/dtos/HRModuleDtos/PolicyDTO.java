package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

@Data
public class PolicyDTO {
    private Long policyId;
    private String policyName;
    private String description;
    private Long departmentId ;

    public PolicyDTO() {

    }

    public Long getDepartmentId() {
        return null;
    }
    public PolicyDTO(Long policyId, String policyName, String description, Long departmentId) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.description = description;
        this.departmentId = departmentId;
    }

}
