package com.bezkoder.springjwt.models;

import com.bezkoder.springjwt.models.HRModuleEntities.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
public class Employee extends User {

    private LocalDate hireDate ;

    @NotNull
    private Double salary;

    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;


    // Un employé peut avoir plusieurs contrats
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contract> contracts;

    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private EEmployeePosition position;

    

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;


    @ManyToOne
    Contract contract ;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Perks> perks;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Payroll> payrolls;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Shift> shift;


    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TimeSheet> timesheets;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Leave> leave;

    public Employee(String name,
                    String lastName,
                    String email,
                    String password,
                    Double salary,
                    LocalDate hireDate,
                    EEmployeePosition position) {
        // On appelle le constructeur parent pour initialiser les champs de User
        super(name, lastName, email, password);
        // Puis on initialise les champs spécifiques à Employee
        this.salary = salary;
        this.hireDate = hireDate;
        this.position = position;
    }

    }



