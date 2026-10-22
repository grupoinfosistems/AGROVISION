package com.agrovision.service;

import com.agrovision.dto.DiseaseInfoDTO;
import com.agrovision.dto.PlantInfoDTO;
import com.agrovision.dto.ProductDTO;
import com.agrovision.model.Product;
import com.agrovision.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductByCode(String code) {
        return productRepository.findByProductCode(code);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByKeyword(keyword.trim()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findMatchesForDiagnosis(PlantInfoDTO plant, DiseaseInfoDTO disease) {
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) {
            return Collections.emptyList();
        }

        String diseaseName = disease != null && disease.getDiseaseName() != null ? disease.getDiseaseName().toLowerCase() : "";
        String eppo = disease != null && disease.getEppoCode() != null ? disease.getEppoCode().toLowerCase() : "";
        String plantName = plant != null && plant.getSpecies() != null ? plant.getSpecies().toLowerCase() : "";

        // Collect search terms from disease
        Set<String> searchTerms = new HashSet<>();
        if (!diseaseName.isEmpty()) {
            // Remove common words
            String clean = diseaseName.replaceAll("(?i)[\\(\\)\\/\\,]", " ");
            for (String word : clean.split("\\s+")) {
                if (word.length() > 3 && !List.of("para", "como", "sobre", "gran", "alta", "baja", "spp.", "spp").contains(word)) {
                    searchTerms.add(word);
                }
            }
        }

        // Add crop terms if available
        if (plant != null && plant.getCommonNames() != null) {
            for (String cn : plant.getCommonNames()) {
                if (cn.length() > 3) {
                    searchTerms.add(cn.toLowerCase());
                }
            }
        }

        // Score products
        Map<Product, Integer> scores = new HashMap<>();
        for (Product p : allProducts) {
            int score = 0;
            String targets = (p.getTargets() != null ? p.getTargets().toLowerCase() : "");
            String crops = (p.getCrops() != null ? p.getCrops().toLowerCase() : "");
            String name = (p.getName() != null ? p.getName().toLowerCase() : "");
            String category = (p.getCategory() != null ? p.getCategory().toLowerCase() : "");

            // Direct disease match
            if (!diseaseName.isEmpty() && targets.contains(diseaseName)) {
                score += 15;
            }

            // Keyword match in targets
            for (String term : searchTerms) {
                if (targets.contains(term)) {
                    score += 8;
                }
                if (crops.contains(term)) {
                    score += 4;
                }
                if (name.contains(term)) {
                    score += 5;
                }
            }

            // Category match (e.g. fungicida if hongo / mildiu / oidio / roya)
            if (diseaseName.contains("oídio") || diseaseName.contains("mildiu") || diseaseName.contains("roya") ||
                diseaseName.contains("botrytis") || diseaseName.contains("antracnosis") || diseaseName.contains("tizón") ||
                diseaseName.contains("cercospora") || diseaseName.contains("alternaria")) {
                if (category.contains("fungicida")) {
                    score += 6;
                }
            } else if (diseaseName.contains("mosca") || diseaseName.contains("pulgón") || diseaseName.contains("gusano") ||
                       diseaseName.contains("trips") || diseaseName.contains("minador") || diseaseName.contains("polilla")) {
                if (category.contains("insecticida")) {
                    score += 6;
                }
            } else if (diseaseName.contains("ácaro") || diseaseName.contains("araña") || diseaseName.contains("arañita")) {
                if (category.contains("acaricida")) {
                    score += 8;
                }
            }

            if (score > 0) {
                scores.put(p, score);
            }
        }

        // Sort descending by score
        List<Product> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(4)
                .collect(Collectors.toList());

        // Fallback: If no match found, return first 3 products from catalog as recommended options
        if (sorted.isEmpty()) {
            sorted = allProducts.stream().limit(3).collect(Collectors.toList());
        }

        return sorted.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductDTO toDto(Product p) {
        if (p == null) return null;
        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getActiveIngredient(),
                p.getCategory(),
                p.getDose(),
                p.getPrice(),
                p.getDescription()
        );
    }
}
