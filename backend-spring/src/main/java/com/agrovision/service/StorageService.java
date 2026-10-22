package com.agrovision.service;

import com.agrovision.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation;

    public StorageService(AppProperties appProperties) {
        this.rootLocation = Paths.get(appProperties.getUploadsDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de subidas", e);
        }
    }

    public String store(MultipartFile file, String id) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No se puede guardar un archivo vacío.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("jpg|jpeg|png|webp")) {
                extension = "jpg";
            }
        }

        String safeFileName = (id != null ? id : UUID.randomUUID().toString()) + "." + extension;

        try {
            Path destinationFile = this.rootLocation.resolve(safeFileName).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new SecurityException("No se puede guardar fuera del directorio designado.");
            }
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return safeFileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el archivo", e);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }
}
