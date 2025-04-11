package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnalyseTempsResponseDto {
    private Long tempsTotalTravailleMinutes;
    private Integer nombrePointages;
    private Long pausesTotalesMinutes;
    private String productivite; // en pourcentage (e.g., "85.50%")
    // Ajoutez d'autres champs si nécessaire (heures supp, détail par type...)
    private List<EntreeDeTempsDTO> pointagesDetail; // Renvoyer les détails
}
