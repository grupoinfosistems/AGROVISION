package com.agrovision.dto;

public class ProductDTO {
    private Long id;
    private String name;
    private String activeIngredient;
    private String category;
    private String dose;
    private String price;
    private String description;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, String activeIngredient, String category, String dose, String price, String description) {
        this.id = id;
        this.name = name;
        this.activeIngredient = activeIngredient;
        this.category = category;
        this.dose = dose;
        this.price = price;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
