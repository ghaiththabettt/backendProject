package com.bezkoder.springjwt.dtos.HRModuleDtos;


import com.bezkoder.springjwt.models.HRModuleEntities.TypeEntreeDeTemps;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StartPointageRequest {
    @NotNull
    private Long employeeId; // Sera souvent extrait du token JWT, mais peut être envoyé si Admin le fait pour qqn d'autre

    private Long tacheId; // Obligatoire dans le form Angular original

    private String notes;

    private TypeEntreeDeTemps typeEntreeDeTemps = TypeEntreeDeTemps.Travail;

    // Pour mode Flexible
    private LocalDateTime heureDebutProposee; // Utiliser LocalDateTime
    private LocalDateTime heureFinProposee;

    // Pour mode Strict (optionnel si récupéré automatiquement au backend)
    private PointageLocationDto localisationDebut;
}

