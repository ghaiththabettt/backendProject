package com.bezkoder.springjwt.payload.response;

import com.bezkoder.springjwt.models.EEmployeePosition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
@Setter
@Getter

public class EmployeeResponse extends UserResponse {


    private Double salary;
    private EEmployeePosition position;

    public EmployeeResponse(Long id, String name, String lastName, String email, 
                          String address, String phoneNumber, byte[] image, 
                          List<String> roles, Double salary,
                          EEmployeePosition position) {
        super(id, name, lastName, email, address, phoneNumber, image, roles);

        this.salary = salary;
        this.position = position;
    }


}
