package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shiftId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;  // Relation avec Employee

    private String shiftName;
    private LocalTime startTime;  // Heure de début du shift
    private LocalTime endTime;    // Heure de fin du shift


}

