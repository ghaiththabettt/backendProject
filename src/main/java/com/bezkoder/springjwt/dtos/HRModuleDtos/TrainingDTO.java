package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TrainingDTO {
    private Long trainingId;
    private String topic;
    private LocalDate startDate;
    private LocalDate endDate;
    private String trainingType;
    private String trainingName ;


    // Exemple : liste d'employés participants (IDs)
    private List<Long> participantIds;
}
