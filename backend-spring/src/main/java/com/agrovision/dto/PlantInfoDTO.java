package com.agrovision.dto;

import java.util.ArrayList;
import java.util.List;

public class PlantInfoDTO {
    private String species;
    private List<String> commonNames = new ArrayList<>();
    private double confidence;
    private String family;

    public PlantInfoDTO() {}

    public PlantInfoDTO(String species, List<String> commonNames, double confidence, String family) {
        this.species = species;
        this.commonNames = commonNames != null ? commonNames : new ArrayList<>();
        this.confidence = confidence;
        this.family = family;
    }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public List<String> getCommonNames() { return commonNames; }
    public void setCommonNames(List<String> commonNames) { this.commonNames = commonNames; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
}
