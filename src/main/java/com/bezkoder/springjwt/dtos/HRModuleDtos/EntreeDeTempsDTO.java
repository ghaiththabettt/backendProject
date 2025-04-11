package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.RestrictionsHorloge;
import com.bezkoder.springjwt.models.HRModuleEntities.Status;
import com.bezkoder.springjwt.models.HRModuleEntities.TypeEntreeDeTemps;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class EntreeDeTempsDTO {
    private Long id;
    private Long employeeId;
    private String employeeFullName; // Pour l'affichage facile
    private Long tacheId;
    private String tacheTitre; // Pour l'affichage facile
    private TypeEntreeDeTemps typeEntreeDeTemps;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;
    private Long dureeNetteMinutes;
    private Long dureePauseMinutes;
    private PointageLocationDto localisationDebut;
    private PointageLocationDto localisationFin;
    private List<PauseDTO> pauses; // Liste des DTOs de pause
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private RestrictionsHorloge restrictionsHorloge;
    private Status status;
}
