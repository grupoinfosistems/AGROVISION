package com.agrovision.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class ImageQualityService {

    public static class QualityResult {
        private final boolean ok;
        private final String reason;

        public QualityResult(boolean ok, String reason) {
            this.ok = ok;
            this.reason = reason;
        }

        public boolean isOk() { return ok; }
        public String getReason() { return reason; }
    }

    public QualityResult evaluate(File imageFile, String organ) {
        if (imageFile == null || !imageFile.exists()) {
            return new QualityResult(false, "El archivo de imagen no existe o está dañado.");
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                return new QualityResult(false, "No se pudo decodificar el formato de la imagen. Use JPG o PNG.");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // 1. Resolución mínima
            if (width < 200 || height < 200) {
                return new QualityResult(false, "La imagen es demasiado pequeña (" + width + "x" + height + "). Acérquese más al cultivo.");
            }

            // 2. Muestreo de brillo y color verde
            long totalBrightness = 0;
            int greenPixels = 0;
            int sampledPixels = 0;

            int step = Math.max(1, (width * height) / 25000); // Muestreo para alto rendimiento

            for (int y = 0; y < height; y += (int) Math.sqrt(step)) {
                for (int x = 0; x < width; x += (int) Math.sqrt(step)) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    totalBrightness += luminance;
                    sampledPixels++;

                    // Detección de tejido vegetal (verde predominante sobre rojo/azul)
                    if (g > 40 && g > r * 1.05 && g > b * 1.05) {
                        greenPixels++;
                    }
                }
            }

            if (sampledPixels == 0) sampledPixels = 1;
            double avgBrightness = (double) totalBrightness / sampledPixels;
            double greenRatio = (double) greenPixels / sampledPixels;

            // 3. Verificación de iluminación
            if (avgBrightness < 25) {
                return new QualityResult(false, "La imagen está muy oscura. Tome la foto con mejor iluminación natural.");
            }
            if (avgBrightness > 248) {
                return new QualityResult(false, "La imagen tiene sobreexposición de luz. Evite apuntar directamente al sol.");
            }

            // 4. Verificación de vegetación para hojas y flores
            if (!"bark".equalsIgnoreCase(organ) && !"tallo".equalsIgnoreCase(organ)) {
                if (greenRatio < 0.02) {
                    return new QualityResult(false, "No se detectó suficiente tejido vegetal en la foto. Enfoque la hoja o fruto afectado.");
                }
            }

            return new QualityResult(true, "Calidad aceptable");

        } catch (IOException e) {
            return new QualityResult(false, "Error al procesar la imagen: " + e.getMessage());
        }
    }
}
