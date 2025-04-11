package com.bezkoder.springjwt.dtos.HRModuleDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ModifierDatesRequest {
    @NotNull
    private Long pointageId;
    private LocalDateTime nouvelleHeureDebut;
    private LocalDateTime nouvelleHeureFin;
}
