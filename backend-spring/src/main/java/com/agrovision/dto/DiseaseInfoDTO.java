package com.agrovision.dto;

import java.util.ArrayList;
import java.util.List;

public class DiseaseInfoDTO {
    private String diseaseName;
    private String eppoCode;
    private double confidence;
    private String description;
    private String source;
    private boolean isUnclassified;
    private List<String> relatedImages = new ArrayList<>();

    public DiseaseInfoDTO() {}

    public DiseaseInfoDTO(String diseaseName, String eppoCode, double confidence, String description, String source) {
        this.diseaseName = diseaseName;
        this.eppoCode = eppoCode;
        this.confidence = confidence;
        this.description = description;
        this.source = source;
    }

    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public String getEppoCode() { return eppoCode; }
    public void setEppoCode(String eppoCode) { this.eppoCode = eppoCode; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isUnclassified() { return isUnclassified; }
    public void setUnclassified(boolean unclassified) { isUnclassified = unclassified; }
    public List<String> getRelatedImages() { return relatedImages; }
    public void setRelatedImages(List<String> relatedImages) { this.relatedImages = relatedImages; }
}
