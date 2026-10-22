package com.agrovision.service;

import com.agrovision.config.AppProperties;
import com.agrovision.dto.DiseaseInfoDTO;
import com.agrovision.dto.PlantInfoDTO;
import com.agrovision.dto.ProductDTO;
import com.agrovision.dto.TreatmentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class OpenAiVisionService {

    private final WebClient webClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAiVisionService(WebClient webClient, AppProperties appProperties) {
        this.webClient = webClient;
        this.appProperties = appProperties;
        this.objectMapper = new ObjectMapper();
    }

    public DiseaseInfoDTO analyzeWithVision(File imageFile, PlantInfoDTO plantInfo, String organ) {
        String apiKey = appProperties.getOpenai().getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || !apiKey.startsWith("sk-")) {
            return null;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            String mimeType = imageFile.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";

            String systemPrompt = "Eres un fitopatólogo experto en sanidad vegetal en cultivos de Perú y Latinoamérica. " +
                    "Analiza la imagen de la planta con daño y responde ÚNICAMENTE con un JSON válido con esta estructura: " +
                    "{\"disease_name\": \"Nombre del problema, plaga o enfermedad\", \"confidence\": 0.85, \"description\": \"Breve descripción de los síntomas visibles\"}";

            String userText = "Planta: " + (plantInfo != null ? plantInfo.getSpecies() : "Cultivo agrícola") +
                    ". Parte afectada: " + organ + ". Identifica la plaga, hongo o enfermedad presente.";

            Map<String, Object> imageUrlMap = Map.of("url", "data:" + mimeType + ";base64," + base64Image);
            List<Map<String, Object>> userContent = List.of(
                    Map.of("type", "text", "text", userText),
                    Map.of("type", "image_url", "image_url", imageUrlMap)
            );

            Map<String, Object> payload = Map.of(
                    "model", appProperties.getOpenai().getVisionModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userContent)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "max_tokens", 500,
                    "temperature", 0.2
            );

            JsonNode response = webClient.post()
                    .uri(OPENAI_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("choices") && response.get("choices").size() > 0) {
                String content = response.get("choices").get(0).path("message").path("content").asText();
                JsonNode parsed = objectMapper.readTree(content);

                String name = parsed.path("disease_name").asText("Problema fitosanitario detectado");
                double confidence = parsed.path("confidence").asDouble(0.75);
                String desc = parsed.path("description").asText("Diagnóstico visual por IA");

                return new DiseaseInfoDTO(name, "VISION_AI", confidence, desc, "gpt-4o-vision");
            }
        } catch (Exception e) {
            System.err.println("[OpenAI Vision] Error: " + e.getMessage());
        }

        return null;
    }

    public TreatmentDTO generateRecommendation(PlantInfoDTO plant, DiseaseInfoDTO disease, WeatherService.WeatherSummary weather, List<ProductDTO> matchedProducts) {
        String apiKey = appProperties.getOpenai().getApiKey();

        if (apiKey != null && apiKey.startsWith("sk-")) {
            try {
                String systemPrompt = "Eres un ingeniero agrónomo especialista en sanidad vegetal en Ica, Perú. " +
                        "Genera una recomendación técnica y clara para el agricultor en formato JSON con la siguiente estructura: " +
                        "{\"recommendation\": \"explicación clara de manejo preventivo y curativo\", \"urgency\": \"baja|media|alta|critica\", \"follow_up_days\": 7}";

                StringBuilder userMsg = new StringBuilder();
                userMsg.append("Cultivo: ").append(plant != null ? plant.getSpecies() : "Planta").append("\n");
                userMsg.append("Diagnóstico: ").append(disease != null ? disease.getDiseaseName() : "Síntoma").append("\n");
                if (weather != null) {
                    userMsg.append("Clima actual: Temp ").append(weather.getTemperatureC()).append("°C, Humedad ")
                            .append(weather.getRelativeHumidity()).append("%, Viento ").append(weather.getWindSpeedKmh()).append(" km/h\n");
                }
                userMsg.append("Insumos disponibles en tienda Chiaway: ");
                matchedProducts.forEach(p -> userMsg.append(p.getName()).append(" (Dosis: ").append(p.getDose()).append("), "));

                Map<String, Object> payload = Map.of(
                        "model", appProperties.getOpenai().getAgentModel(),
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userMsg.toString())
                        ),
                        "response_format", Map.of("type", "json_object"),
                        "max_tokens", 450,
                        "temperature", 0.3
                );

                JsonNode response = webClient.post()
                        .uri(OPENAI_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();

                if (response != null && response.has("choices") && response.get("choices").size() > 0) {
                    String content = response.get("choices").get(0).path("message").path("content").asText();
                    JsonNode parsed = objectMapper.readTree(content);

                    String rec = parsed.path("recommendation").asText();
                    String urgency = parsed.path("urgency").asText("media");
                    int followUp = parsed.path("follow_up_days").asInt(7);

                    return new TreatmentDTO("gpt-4o-mini", rec, urgency, followUp, matchedProducts);
                }
            } catch (Exception e) {
                System.err.println("[OpenAI Recommendation] Fallback a regla local: " + e.getMessage());
            }
        }

        // Fallback local robusto basado en catálogo
        String rec = "Se detectó presencia de síntomas de " + (disease != null ? disease.getDiseaseName() : "afectación") +
                ". Se sugiere realizar aplicación foliar en las primeras horas de la mañana, verificar la calibración del equipo y rotar ingredientes activos para prevenir resistencias.";
        String urgency = (disease != null && disease.getConfidence() >= 0.75) ? "alta" : "media";
        return new TreatmentDTO("regla-local", rec, urgency, 7, matchedProducts);
    }
}
