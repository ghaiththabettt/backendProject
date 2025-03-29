package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class ContractDTO {
    private Long contractId;
    private String contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate renewalDate;
    private String reference;
}
