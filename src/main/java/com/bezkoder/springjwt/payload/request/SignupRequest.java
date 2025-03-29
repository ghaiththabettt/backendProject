package com.bezkoder.springjwt.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.bezkoder.springjwt.models.EUserType;
import com.bezkoder.springjwt.models.EEmployeePosition;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class SignupRequest {
    // Getters and Setters
    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @Size(max = 200)
    private String address;

    @Size(max = 20)
    private String phoneNumber;

    private EUserType userType;

    // For Customer
    private String companyName;

    // For Employee
    private Double salary;
    private EEmployeePosition position;


    public Object getHireDate() {
        return null;
    }
}
