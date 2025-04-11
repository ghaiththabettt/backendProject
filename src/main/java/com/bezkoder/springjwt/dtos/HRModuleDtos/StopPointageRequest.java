package com.bezkoder.springjwt.dtos.HRModuleDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StopPointageRequest {
    @NotNull
    private Long pointageId; // ID de l'EntreeDeTemps à arrêter

    // Pour mode Strict (ou autres validations)
    private Boolean gpsValide;
    private Boolean reconnaissanceFacialeValidee;

    // Pour mode Strict/Modéré afin d'ajouter la localisation de fin
    private PointageLocationDto localisationFin;
}