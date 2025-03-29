package com.bezkoder.springjwt.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserResponse {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String address;
    private String phoneNumber;
    private byte[] image;
    private List<String> roles;

    public UserResponse(Long id, String name, String lastName, String email, 
                       String address, String phoneNumber, byte[] image, List<String> roles) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.roles = roles;
    }

}
