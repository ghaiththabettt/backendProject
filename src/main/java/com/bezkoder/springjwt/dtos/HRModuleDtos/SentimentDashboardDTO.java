package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SentimentDashboardDTO {
    // Pour le graphique circulaire des sentiments
    private Map<String, Long> sentimentCounts; // Ex: {"🟢 Positif": 50, "🔴 Négatif": 10, "🟡 Neutre": 20}

    // Pour la courbe de motivation (par mois par exemple)
    private List<MotivationTrendPoint> motivationTrend;
}