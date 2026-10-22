package com.agrovision.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "client_name", length = 150)
    private String clientName;

    @Column(name = "client_email", length = 150)
    private String clientEmail;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String organ;

    @Column(name = "plant_species", length = 200)
    private String plantSpecies;

    @Column(name = "plant_common_name", length = 200)
    private String plantCommonName;

    @Column(name = "plant_confidence")
    private Double plantConfidence;

    @Column(name = "disease_name", length = 250)
    private String diseaseName;

    @Column(name = "eppo_code", length = 50)
    private String eppoCode;

    @Column(name = "disease_confidence")
    private Double diseaseConfidence;

    @Column(length = 50)
    private String urgency; // baja, media, alta, critica

    @Column(name = "follow_up_days")
    private Integer followUpDays;

    @Column(length = 50)
    private String source; // plantnet, gpt-4o-vision, template

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "needs_expert")
    private Boolean needsExpert = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Diagnosis() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getOrgan() { return organ; }
    public void setOrgan(String organ) { this.organ = organ; }
    public String getPlantSpecies() { return plantSpecies; }
    public void setPlantSpecies(String plantSpecies) { this.plantSpecies = plantSpecies; }
    public String getPlantCommonName() { return plantCommonName; }
    public void setPlantCommonName(String plantCommonName) { this.plantCommonName = plantCommonName; }
    public Double getPlantConfidence() { return plantConfidence; }
    public void setPlantConfidence(Double plantConfidence) { this.plantConfidence = plantConfidence; }
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    public String getEppoCode() { return eppoCode; }
    public void setEppoCode(String eppoCode) { this.eppoCode = eppoCode; }
    public Double getDiseaseConfidence() { return diseaseConfidence; }
    public void setDiseaseConfidence(Double diseaseConfidence) { this.diseaseConfidence = diseaseConfidence; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public Integer getFollowUpDays() { return followUpDays; }
    public void setFollowUpDays(Integer followUpDays) { this.followUpDays = followUpDays; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public Boolean getNeedsExpert() { return needsExpert; }
    public void setNeedsExpert(Boolean needsExpert) { this.needsExpert = needsExpert; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
