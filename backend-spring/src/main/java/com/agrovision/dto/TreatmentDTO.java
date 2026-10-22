package com.agrovision.dto;

import java.util.ArrayList;
import java.util.List;

public class TreatmentDTO {
    private String source;
    private String recommendation;
    private String urgency; // baja, media, alta, critica
    private int followUpDays;
    private List<ProductDTO> products = new ArrayList<>();

    public TreatmentDTO() {}

    public TreatmentDTO(String source, String recommendation, String urgency, int followUpDays, List<ProductDTO> products) {
        this.source = source;
        this.recommendation = recommendation;
        this.urgency = urgency;
        this.followUpDays = followUpDays;
        this.products = products != null ? products : new ArrayList<>();
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public int getFollowUpDays() { return followUpDays; }
    public void setFollowUpDays(int followUpDays) { this.followUpDays = followUpDays; }
    public List<ProductDTO> getProducts() { return products; }
    public void setProducts(List<ProductDTO> products) { this.products = products; }
}
