package com.bezkoder.springjwt.dtos.HRModuleDtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReprendreRequest {
    @NotNull
    private Long pointageId; // ID de l'EntreeDeTemps à reprendre
}
