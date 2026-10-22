package com.agrovision.service;

import com.agrovision.config.AppProperties;
import com.agrovision.dto.DiseaseInfoDTO;
import com.agrovision.dto.PlantInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.util.*;

@Service
public class PlantNetService {

    private final WebClient webClient;
    private final AppProperties appProperties;

    private static final Map<String, String> EPPO_READABLE = new HashMap<>();
    static {
        EPPO_READABLE.put("UNCISO", "Oídio (Erysiphe necator / Powdery Mildew)");
        EPPO_READABLE.put("PLASVI", "Mildiu de la vid (Plasmopara viticola)");
        EPPO_READABLE.put("BOTRCN", "Botrytis / pudrición gris (Botrytis cinerea)");
        EPPO_READABLE.put("CERCCN", "Cercospora (mancha foliar)");
        EPPO_READABLE.put("ALTESO", "Alternaria (mancha foliar oscura)");
        EPPO_READABLE.put("COLLSP", "Colletotrichum / antracnosis");
        EPPO_READABLE.put("SEPTSP", "Septoria (mancha de la hoja)");
        EPPO_READABLE.put("PUCCSP", "Roya (Puccinia spp.)");
        EPPO_READABLE.put("PHYTSP", "Phytophthora / tizón");
        EPPO_READABLE.put("FUSAOX", "Fusarium (marchitez vascular)");
        EPPO_READABLE.put("1APHIF", "Pulgones / áfidos (Aphididae)");
        EPPO_READABLE.put("APHISP", "Pulgones (Aphis spp.)");
        EPPO_READABLE.put("APHIGO", "Pulgón del algodón / melón (Aphis gossypii)");
        EPPO_READABLE.put("1ACARI", "Ácaros (Acari)");
        EPPO_READABLE.put("1TETRF", "Arañas rojas (Tetranychidae)");
        EPPO_READABLE.put("TETTUR", "Araña roja (Tetranychus urticae)");
        EPPO_READABLE.put("1ALEYF", "Mosca blanca (Aleyrodidae)");
        EPPO_READABLE.put("BEMITA", "Mosca blanca (Bemisia tabaci)");
        EPPO_READABLE.put("1THYSF", "Trips (Thysanoptera)");
        EPPO_READABLE.put("FRANOC", "Trips de las flores (Frankliniella occidentalis)");
        EPPO_READABLE.put("1COCCF", "Cochinillas (Coccidae)");
        EPPO_READABLE.put("PSEUDC", "Cochinilla harinosa (Pseudococcus spp.)");
        EPPO_READABLE.put("SPODFU", "Gusano cogollero del maíz (Spodoptera frugiperda)");
        EPPO_READABLE.put("HELIAR", "Gusano del fruto (Helicoverpa armigera)");
        EPPO_READABLE.put("LIRISP", "Minador de hojas (Liriomyza spp.)");
    }

    public PlantNetService(WebClient webClient, AppProperties appProperties) {
        this.webClient = webClient;
        this.appProperties = appProperties;
    }

    public PlantInfoDTO identifyPlant(File imageFile, String organ, String plantHint) {
        if (plantHint != null && !plantHint.trim().isEmpty()) {
            return new PlantInfoDTO(plantHint.trim(), List.of(plantHint.trim()), 0.95, "Cultivo especificado");
        }

        String apiKey = appProperties.getPlantnet().getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new PlantInfoDTO("Planta no especificada", List.of(), 0.50, "");
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("images", new FileSystemResource(imageFile));
            builder.part("organs", organ != null ? organ : "leaf");

            JsonNode root = webClient.post()
                    .uri(appProperties.getPlantnet().getBaseUrl() + "/identify/all?api-key=" + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root != null && root.has("results") && root.get("results").size() > 0) {
                JsonNode top = root.get("results").get(0);
                double score = top.path("score").asDouble(0.0);
                JsonNode speciesNode = top.path("species");
                String scientificName = speciesNode.path("scientificNameWithoutAuthor").asText("Desconocida");
                String family = speciesNode.path("family").path("scientificNameWithoutAuthor").asText("");

                List<String> commonNames = new ArrayList<>();
                if (speciesNode.has("commonNames")) {
                    speciesNode.get("commonNames").forEach(c -> commonNames.add(c.asText()));
                }

                return new PlantInfoDTO(scientificName, commonNames, score, family);
            }
        } catch (Exception e) {
            System.err.println("[PlantNet] Error identificando planta: " + e.getMessage());
        }

        return new PlantInfoDTO("Cultivo local", List.of("Planta agrícola"), 0.60, "");
    }

    public DiseaseInfoDTO identifyDisease(File imageFile, String organ) {
        String apiKey = appProperties.getPlantnet().getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("images", new FileSystemResource(imageFile));
            builder.part("organs", organ != null ? organ : "leaf");

            JsonNode root = webClient.post()
                    .uri(appProperties.getPlantnet().getBaseUrl() + "/identify/k-diseases?api-key=" + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root != null && root.has("results") && root.get("results").size() > 0) {
                JsonNode top = root.get("results").get(0);
                double score = top.path("score").asDouble(0.0);
                JsonNode speciesNode = top.path("species");
                String rawName = speciesNode.path("scientificNameWithoutAuthor").asText("Síntoma");
                String eppo = "";

                if (speciesNode.has("name")) {
                    eppo = speciesNode.path("name").asText("").toUpperCase();
                }

                String readable = EPPO_READABLE.getOrDefault(eppo, rawName);
                boolean unclassified = eppo.startsWith("ZZ") || readable.contains("ZZ") || score < 0.20;

                DiseaseInfoDTO dto = new DiseaseInfoDTO(readable, eppo, score, "Identificado por Pl@ntNet API v2", "plantnet");
                dto.setUnclassified(unclassified);
                return dto;
            }
        } catch (Exception e) {
            System.err.println("[PlantNet] Error identificando patógeno: " + e.getMessage());
        }

        return null;
    }
}
