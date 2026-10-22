package com.agrovision.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AgroVision AI Spring Boot",
                "version", "3.3.4",
                "timestamp", LocalDateTime.now().toString(),
                "javaVersion", System.getProperty("java.version"),
                "architecture", "Layered (Controller / Service / Repository / Model)"
        ));
    }
}
