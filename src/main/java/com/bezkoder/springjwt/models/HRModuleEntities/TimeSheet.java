package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "timesheets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timesheetId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Relation avec Employee

    private LocalDate date;              // Date de travail
    private float hoursWorked;           // Nombre d'heures travaillées
    private String taskDescription;      // Description de la tâche effectuée

    /*@Enumerated(EnumType.STRING)
    private String approvalStatus;  */
}
