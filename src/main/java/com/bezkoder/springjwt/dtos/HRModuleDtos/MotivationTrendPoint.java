package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MotivationTrendPoint {
    private String period; // Ex: "2023-01", "2023-02"
    private double score;  // Score moyen de motivation pour cette période
}