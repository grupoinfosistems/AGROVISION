package com.agrovision.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "agrovision")
public class AppProperties {

    private PlantNet plantnet = new PlantNet();
    private OpenAi openai = new OpenAi();
    private double confidenceThreshold = 0.70;
    private String uploadsDir = "uploads";
    private Cors cors = new Cors();

    public static class PlantNet {
        private String apiKey = "";
        private String baseUrl = "https://my-api.plantnet.org/v2";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class OpenAi {
        private String apiKey = "";
        private String visionModel = "gpt-4o";
        private String agentModel = "gpt-4o-mini";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getVisionModel() { return visionModel; }
        public void setVisionModel(String visionModel) { this.visionModel = visionModel; }
        public String getAgentModel() { return agentModel; }
        public void setAgentModel(String agentModel) { this.agentModel = agentModel; }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public PlantNet getPlantnet() { return plantnet; }
    public void setPlantnet(PlantNet plantnet) { this.plantnet = plantnet; }
    public OpenAi getOpenai() { return openai; }
    public void setOpenai(OpenAi openai) { this.openai = openai; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
    public String getUploadsDir() { return uploadsDir; }
    public void setUploadsDir(String uploadsDir) { this.uploadsDir = uploadsDir; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
}
