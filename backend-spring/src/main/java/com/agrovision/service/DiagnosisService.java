package com.agrovision.service;

import com.agrovision.dto.*;
import com.agrovision.model.Diagnosis;
import com.agrovision.repository.DiagnosisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DiagnosisService {

    private final StorageService storageService;
    private final ImageQualityService imageQualityService;
    private final PlantNetService plantNetService;
    private final OpenAiVisionService openAiVisionService;
    private final WeatherService weatherService;
    private final ProductService productService;
    private final DiagnosisRepository diagnosisRepository;

    public DiagnosisService(StorageService storageService,
                            ImageQualityService imageQualityService,
                            PlantNetService plantNetService,
                            OpenAiVisionService openAiVisionService,
                            WeatherService weatherService,
                            ProductService productService,
                            DiagnosisRepository diagnosisRepository) {
        this.storageService = storageService;
        this.imageQualityService = imageQualityService;
        this.plantNetService = plantNetService;
        this.openAiVisionService = openAiVisionService;
        this.weatherService = weatherService;
        this.productService = productService;
        this.diagnosisRepository = diagnosisRepository;
    }

    @Transactional
    public DiagnosisResponseDTO diagnose(MultipartFile file,
                                         String organ,
                                         String plantHint,
                                         String clientName,
                                         String clientEmail,
                                         Double lat,
                                         Double lon) {
        // 1. Generate diagnosis ID and store uploaded image
        String diagnosisId = "diag_" + UUID.randomUUID().toString().substring(0, 12);
        String savedFilename = storageService.store(file, diagnosisId);
        Path filePath = storageService.load(savedFilename);
        File imageFile = filePath.toFile();

        // 2. Validate image quality
        ImageQualityService.QualityResult quality = imageQualityService.evaluate(imageFile, organ);
        if (!quality.isOk()) {
            return DiagnosisResponseDTO.qualityFailed(
                    quality.getReason(),
                    "Por favor tome una foto cercana, bien enfocada, iluminada con luz natural y mostrando las partes afectadas de la hoja, fruto o tallo."
            );
        }

        // 3. Identify Plant (Pl@ntNet or hint)
        PlantInfoDTO plant = plantNetService.identifyPlant(imageFile, organ, plantHint);

        // 4. Identify Disease / Pest (Pl@ntNet with fallback to OpenAI Vision)
        DiseaseInfoDTO disease = plantNetService.identifyDisease(imageFile, organ);

        boolean needsExpert = false;
        if (disease == null || disease.isUnclassified() || disease.getConfidence() < 0.25) {
            // Fallback to OpenAI Vision
            DiseaseInfoDTO aiDisease = openAiVisionService.analyzeWithVision(imageFile, plant, organ);
            if (aiDisease != null) {
                disease = aiDisease;
            } else {
                disease = new DiseaseInfoDTO(
                        "Síntomas indeterminados / Consulta fitopatológica recomendada",
                        "UNCLASSIFIED",
                        0.40,
                        "Se detectaron anomalías foliares, pero se requiere confirmación por un ingeniero agrónomo especialista.",
                        "regla-experta"
                );
                disease.setUnclassified(true);
                needsExpert = true;
            }
        }

        if (disease.getConfidence() < 0.35) {
            needsExpert = true;
        }

        // 5. Match Peruvian catalog products
        List<ProductDTO> matchedProducts = productService.findMatchesForDiagnosis(plant, disease);

        // 6. Query weather (default to Lima coast coordinates if not provided)
        double queryLat = (lat != null) ? lat : -12.0464;
        double queryLon = (lon != null) ? lon : -77.0428;
        WeatherService.WeatherSummary weather = weatherService.getWeather(queryLat, queryLon);

        // 7. Generate actionable treatment and recommendations
        TreatmentDTO treatment = openAiVisionService.generateRecommendation(plant, disease, weather, matchedProducts);

        // 8. Persist diagnosis record into MySQL
        Diagnosis entity = new Diagnosis();
        entity.setId(diagnosisId);
        entity.setClientName(clientName);
        entity.setClientEmail(clientEmail);
        entity.setImageUrl("/uploads/" + savedFilename);
        entity.setOrgan(organ != null ? organ : "leaf");

        if (plant != null) {
            entity.setPlantSpecies(plant.getSpecies());
            entity.setPlantCommonName(plant.getCommonNames() != null && !plant.getCommonNames().isEmpty()
                    ? String.join(", ", plant.getCommonNames()) : "");
            entity.setPlantConfidence(plant.getConfidence());
        }

        if (disease != null) {
            entity.setDiseaseName(disease.getDiseaseName());
            entity.setEppoCode(disease.getEppoCode());
            entity.setDiseaseConfidence(disease.getConfidence());
            entity.setSource(disease.getSource());
        }

        if (treatment != null) {
            entity.setUrgency(treatment.getUrgency());
            entity.setFollowUpDays(treatment.getFollowUpDays());
            entity.setRecommendation(treatment.getRecommendation());
        }

        entity.setNeedsExpert(needsExpert);
        entity.setCreatedAt(LocalDateTime.now());

        diagnosisRepository.save(entity);

        // 9. Build response DTO
        DiagnosisResponseDTO response = new DiagnosisResponseDTO();
        response.setQualityOk(true);
        response.setDiagnosisId(diagnosisId);
        response.setImageUrl("/uploads/" + savedFilename);
        response.setNeedsExpert(needsExpert);
        response.setPlantIdentified(plant);
        response.setDiagnosis(disease);
        response.setTreatment(treatment);

        return response;
    }

    @Transactional(readOnly = true)
    public Optional<Diagnosis> getDiagnosisById(String id) {
        return diagnosisRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Diagnosis> getAllDiagnoses() {
        return diagnosisRepository.findAllByOrderByCreatedAtDesc();
    }
}
