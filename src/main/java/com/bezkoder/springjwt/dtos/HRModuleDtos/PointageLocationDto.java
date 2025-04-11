package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointageLocationDto {
    private Double lat;
    private Double lng;
    private String adresseComplete;
}
