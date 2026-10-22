package com.agrovision.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "quote_items")
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_request_id", nullable = false)
    @JsonIgnore
    private QuoteRequest quoteRequest;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(length = 100)
    private String dose;

    @Column(length = 100)
    private String price;

    public QuoteItem() {}

    public QuoteItem(Long productId, String productName, String dose, String price) {
        this.productId = productId;
        this.productName = productName;
        this.dose = dose;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public QuoteRequest getQuoteRequest() { return quoteRequest; }
    public void setQuoteRequest(QuoteRequest quoteRequest) { this.quoteRequest = quoteRequest; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}
