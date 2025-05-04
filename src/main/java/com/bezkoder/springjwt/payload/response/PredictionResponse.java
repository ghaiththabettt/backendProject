package com.bezkoder.springjwt.payload.response;

public class PredictionResponse {

    private String type_conge_prevu;
    private String sentiment;
    private String interpretation;
    private double confidence;

    public String getType_conge_prevu() {
        return type_conge_prevu;
    }

    public void setType_conge_prevu(String type_conge_prevu) {
        this.type_conge_prevu = type_conge_prevu;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
