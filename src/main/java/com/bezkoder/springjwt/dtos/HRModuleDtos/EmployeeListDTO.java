package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListDTO {
    private Long id;
    private String fullName; // Ou name/lastName séparés si vous préférez
}