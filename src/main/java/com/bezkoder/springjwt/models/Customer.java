package com.bezkoder.springjwt.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity

@Table(name = "customers")
public class Customer extends User {
    
    @Size(max = 100)
    private String companyName;

    public Customer() {
        super();
        setUserType(EUserType.ROLE_CUSTOMER);
    }

    public Customer(String name, String lastName, String email, String password, String companyName) {
        super(name, lastName, email, password);
        this.companyName = companyName;
        setUserType(EUserType.ROLE_CUSTOMER);
    }

}
