package com.agrovision.dto;

import java.util.ArrayList;
import java.util.List;

public class QuoteRequestDTO {
    private String diagnosisId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String notes;
    private List<ProductItemDTO> products = new ArrayList<>();

    public static class ProductItemDTO {
        private Long id;
        private String name;
        private String dose;
        private String price;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDose() { return dose; }
        public void setDose(String dose) { this.dose = dose; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
    }

    public String getDiagnosisId() { return diagnosisId; }
    public void setDiagnosisId(String diagnosisId) { this.diagnosisId = diagnosisId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<ProductItemDTO> getProducts() { return products; }
    public void setProducts(List<ProductItemDTO> products) { this.products = products; }
}
