package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.TypePause;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PauseDTO {
    private Long id;
    private LocalDateTime debut; // <-- CHANGER Date par LocalDateTime
    private LocalDateTime fin;   // <-- CHANGER Date par LocalDateTime
    private TypePause typePause; // <-- CHANGER String par TypePause (importer l'enum)
    private String note;
    private Long dureeMinutes; // <-- AJOUTER pour pouvoir mapper la durée calculée
}
