package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private LocalDate hireDate;
    private Double salary;
    private Date dateOfBirth;
    private String position;
    private Long departmentId;
    private Long contractId;
    private String address ;
    private String phoneNumber;


}
