package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.payload.response.PredictionResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CongePredictionService {

    private static final String FLASK_API_URL = "http://127.0.0.1:5000/predict";

    public PredictionResponse predictTypeCongeAndSentiment(String reason) {
        RestTemplate restTemplate = new RestTemplate();

        // Créer la requête JSON
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("motif_conge", reason);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);

        try {
            ResponseEntity<PredictionResponse> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    request,
                    PredictionResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
