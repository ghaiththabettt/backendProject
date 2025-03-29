package com.bezkoder.springjwt.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class CustomerResponse extends UserResponse {
    private String companyName;

    public CustomerResponse(Long id, String name, String lastName, String email, 
                          String address, String phoneNumber, byte[] image, 
                          List<String> roles, String companyName) {
        super(id, name, lastName, email, address, phoneNumber, image, roles);
        this.companyName = companyName;
    }


}
