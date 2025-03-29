package com.bezkoder.springjwt.payload.request;

import com.bezkoder.springjwt.models.EEmployeePosition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSignupRequest extends SignupRequest {


    @NotNull
    private LocalDate hireDate;

    @NotNull
    private Date dateOfBirth;


    @NotNull
    private Long departmentId;

    @NotNull
    private Long contractId;
}


