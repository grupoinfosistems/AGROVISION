package com.agrovision.controller;

import com.agrovision.dto.DiagnosisResponseDTO;
import com.agrovision.model.Diagnosis;
import com.agrovision.service.DiagnosisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiagnosisResponseDTO> createDiagnosis(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "organ", defaultValue = "leaf") String organ,
            @RequestParam(value = "plantHint", required = false) String plantHint,
            @RequestParam(value = "clientName", required = false) String clientName,
            @RequestParam(value = "clientEmail", required = false) String clientEmail,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DiagnosisResponseDTO.qualityFailed("No se recibió ningún archivo de imagen.", "Adjunte una fotografía clara de la planta.")
            );
        }

        DiagnosisResponseDTO response = diagnosisService.diagnose(
                file, organ, plantHint, clientName, clientEmail, lat, lon
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Diagnosis> getDiagnosisById(@PathVariable String id) {
        return diagnosisService.getDiagnosisById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Diagnosis>> getAllDiagnoses() {
        return ResponseEntity.ok(diagnosisService.getAllDiagnoses());
    }
}
