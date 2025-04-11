package com.bezkoder.springjwt.models.HRModuleEntities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainingId;

    private String trainingName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String trainer;
    private String location;
    private String trainingType;


    // Relation avec Department : Un département organise plusieurs formations
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonBackReference
    private Department department;
}
