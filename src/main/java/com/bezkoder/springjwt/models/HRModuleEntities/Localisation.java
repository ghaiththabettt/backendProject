package com.bezkoder.springjwt.models.HRModuleEntities;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Localisation {
    private Double lat;
    private Double lng;
    private String adresseComplete;
}