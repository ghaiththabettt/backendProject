package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.RestrictionsHorloge;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DernierModeResponseDto {
    private RestrictionsHorloge restrictionsHorloge;
}
