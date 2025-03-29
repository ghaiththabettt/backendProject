package com.bezkoder.springjwt.models.HRModuleEntities;

import com.bezkoder.springjwt.models.Employee;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "perks")
public class Perks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long perksId;

    // Relation ManyToOne si plusieurs perks pour un même employé
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private PerksType perksType; // AWARD, BENEFITS, etc.

    private LocalDate datePerks;
    private String reason;
}
