package com.agrovision.controller;

import com.agrovision.dto.QuoteRequestDTO;
import com.agrovision.model.QuoteRequest;
import com.agrovision.service.QuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ResponseEntity<QuoteRequest> createQuote(@RequestBody QuoteRequestDTO dto) {
        if (dto.getClientName() == null || dto.getClientName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        QuoteRequest saved = quoteService.createQuote(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<QuoteRequest>> getAllQuotes() {
        return ResponseEntity.ok(quoteService.getAllQuotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteRequest> getQuoteById(@PathVariable String id) {
        return quoteService.getQuoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
