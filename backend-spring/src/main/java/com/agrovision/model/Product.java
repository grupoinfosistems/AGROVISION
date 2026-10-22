package com.agrovision.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, length = 100)
    private String productCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "active_ingredient", nullable = false, length = 255)
    private String activeIngredient;

    @Column(nullable = false, length = 50)
    private String category; // fungicida, insecticida, acaricida, biologico, etc.

    @Column(length = 100)
    private String formulation;

    @Column(length = 100)
    private String dose;

    @Column(columnDefinition = "TEXT")
    private String targets; // Plagas o enfermedades objetivo

    @Column(columnDefinition = "TEXT")
    private String crops; // Cultivos indicados (Vid, Palta, etc.)

    @Column(name = "application_interval", length = 255)
    private String applicationInterval;

    @Column(length = 100)
    private String price;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Product() {}

    public Product(String productCode, String name, String activeIngredient, String category, String formulation, String dose, String targets, String crops, String applicationInterval, String price, String description) {
        this.productCode = productCode;
        this.name = name;
        this.activeIngredient = activeIngredient;
        this.category = category;
        this.formulation = formulation;
        this.dose = dose;
        this.targets = targets;
        this.crops = crops;
        this.applicationInterval = applicationInterval;
        this.price = price;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFormulation() { return formulation; }
    public void setFormulation(String formulation) { this.formulation = formulation; }
    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }
    public String getTargets() { return targets; }
    public void setTargets(String targets) { this.targets = targets; }
    public String getCrops() { return crops; }
    public void setCrops(String crops) { this.crops = crops; }
    public String getApplicationInterval() { return applicationInterval; }
    public void setApplicationInterval(String applicationInterval) { this.applicationInterval = applicationInterval; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
