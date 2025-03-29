package com.bezkoder.springjwt.models.HRModuleEntities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "job_positions")
@AllArgsConstructor
@NoArgsConstructor
public class JobPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobPositionId;

    private String title;
    private String jobLevel;
    private String description;


    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false) // Clé étrangère vers Department
    private Department department;


    public Long getId() {
        return null;
    }

    public void setJobPositionId(Long id) {

    }
}
