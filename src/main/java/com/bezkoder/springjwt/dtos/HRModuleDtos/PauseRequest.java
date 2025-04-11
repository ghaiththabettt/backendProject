package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.TypePause;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PauseRequest {
    @NotNull
    private Long pointageId; // ID de l'EntreeDeTemps à mettre en pause

    @NotNull
    private TypePause typePause;

    private String note;
}
