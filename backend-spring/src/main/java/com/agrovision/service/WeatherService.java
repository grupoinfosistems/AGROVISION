package com.agrovision.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {

    private final WebClient webClient;
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";

    public static class WeatherSummary {
        private double temperatureC;
        private int relativeHumidity;
        private double windSpeedKmh;
        private double rainMm;

        public WeatherSummary(double temperatureC, int relativeHumidity, double windSpeedKmh, double rainMm) {
            this.temperatureC = temperatureC;
            this.relativeHumidity = relativeHumidity;
            this.windSpeedKmh = windSpeedKmh;
            this.rainMm = rainMm;
        }

        public double getTemperatureC() { return temperatureC; }
        public int getRelativeHumidity() { return relativeHumidity; }
        public double getWindSpeedKmh() { return windSpeedKmh; }
        public double getRainMm() { return rainMm; }
    }

    public WeatherService(WebClient webClient) {
        this.webClient = webClient;
    }

    public WeatherSummary getWeather(double latitude, double longitude) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.open-meteo.com")
                            .path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current", "temperature_2m,relative_humidity_2m,rain,wind_speed_10m")
                            .queryParam("timezone", "America/Lima")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("current")) {
                JsonNode current = response.get("current");
                double temp = current.path("temperature_2m").asDouble(24.0);
                int humidity = current.path("relative_humidity_2m").asInt(65);
                double wind = current.path("wind_speed_10m").asDouble(10.0);
                double rain = current.path("rain").asDouble(0.0);
                return new WeatherSummary(temp, humidity, wind, rain);
            }
        } catch (Exception e) {
            // Silencioso con valores por defecto de Ica, Perú
        }
        return new WeatherSummary(24.0, 65, 10.0, 0.0);
    }
}
