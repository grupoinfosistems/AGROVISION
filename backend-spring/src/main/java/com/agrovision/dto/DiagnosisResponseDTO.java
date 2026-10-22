package com.agrovision.dto;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisResponseDTO {
    private boolean qualityOk;
    private String reason;
    private String tip;
    private String diagnosisId;
    private String imageUrl;
    private boolean needsExpert;

    private PlantInfoDTO plantIdentified;
    private DiseaseInfoDTO diagnosis;
    private TreatmentDTO treatment;

    public DiagnosisResponseDTO() {}

    public static DiagnosisResponseDTO qualityFailed(String reason, String tip) {
        DiagnosisResponseDTO dto = new DiagnosisResponseDTO();
        dto.setQualityOk(false);
        dto.setReason(reason);
        dto.setTip(tip);
        return dto;
    }

    public boolean isQualityOk() { return qualityOk; }
    public void setQualityOk(boolean qualityOk) { this.qualityOk = qualityOk; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }
    public String getDiagnosisId() { return diagnosisId; }
    public void setDiagnosisId(String diagnosisId) { this.diagnosisId = diagnosisId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isNeedsExpert() { return needsExpert; }
    public void setNeedsExpert(boolean needsExpert) { this.needsExpert = needsExpert; }
    public PlantInfoDTO getPlantIdentified() { return plantIdentified; }
    public void setPlantIdentified(PlantInfoDTO plantIdentified) { this.plantIdentified = plantIdentified; }
    public DiseaseInfoDTO getDiagnosis() { return diagnosis; }
    public void setDiagnosis(DiseaseInfoDTO diagnosis) { this.diagnosis = diagnosis; }
    public TreatmentDTO getTreatment() { return treatment; }
    public void setTreatment(TreatmentDTO treatment) { this.treatment = treatment; }
}
