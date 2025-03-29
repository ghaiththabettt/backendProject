package com.bezkoder.springjwt.models.HRModuleEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    private String title;
    private String description;
    private LocalDate createdDate;
    private LocalDate updatedDate;

    // Relation avec Department : Une policy appartient à un seul département
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
