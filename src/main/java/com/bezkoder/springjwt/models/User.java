package com.bezkoder.springjwt.models;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users", 
    uniqueConstraints = { 
      @UniqueConstraint(columnNames = "email") 
    })
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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
  @Size(max = 120)
  private String password;

  @Size(max = 200)
  private String address;

  @Size(max = 20)
  private String phoneNumber;

  @Lob
  @Column(columnDefinition = "MEDIUMBLOB")
  private byte[] image;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type")
  private EUserType userType;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  public User() {
  }

  public User(String name, String lastName, String email, String password) {
    this.name = name;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
  }

}
