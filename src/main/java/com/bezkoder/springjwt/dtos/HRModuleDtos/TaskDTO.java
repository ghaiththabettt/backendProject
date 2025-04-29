package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor // Constructeur par défaut (utile pour certains frameworks/librairies)
@AllArgsConstructor // Constructeur avec tous les champs (pratique pour les tests/création)
public class TaskDTO {

    private Long taskId;
    private String taskName;
    private String description;

    // Il est souvent préférable d'utiliser String pour les enums dans les DTOs
    // pour la flexibilité côté client. La conversion se fait dans le service/mapper.
    private String status; // Ex: "PENDING", "COMPLETED"

    private LocalDate completionDate; // La date de complétion est utile

    // Informations sur l'employé assigné (on ne veut pas l'objet Employee entier)
    private Long employeeId; // L'ID de l'employé
    private String employeeFullName; // Optionnel mais pratique : Nom + Prénom de l'employé
 }
