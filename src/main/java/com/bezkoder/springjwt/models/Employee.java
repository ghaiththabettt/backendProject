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
import java.util.*;

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


    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntreeDeTemps> entreeDeTempsList;


    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;


    @ManyToOne
    Contract contract ;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Perks> perks;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
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

    @OneToMany(
            mappedBy = "employee", // Doit correspondre au nom du champ dans Task
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY // LAZY est souvent préférable pour les OneToMany
    )
    @JsonIgnore // Important pour éviter les boucles JSON
    private List<Task> tasks = new ArrayList<>(); // Initialiser la liste est crucial




     public void addTask(Task task) {
        tasks.add(task);
        task.setEmployee(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setEmployee(null);
    }

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



