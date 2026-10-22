package com.agrovision.service;

import com.agrovision.dto.QuoteRequestDTO;
import com.agrovision.model.QuoteItem;
import com.agrovision.model.QuoteRequest;
import com.agrovision.repository.QuoteRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuoteService {

    private final QuoteRequestRepository quoteRequestRepository;

    public QuoteService(QuoteRequestRepository quoteRequestRepository) {
        this.quoteRequestRepository = quoteRequestRepository;
    }

    @Transactional
    public QuoteRequest createQuote(QuoteRequestDTO dto) {
        QuoteRequest request = new QuoteRequest();
        String quoteId = "cot_" + UUID.randomUUID().toString().substring(0, 10);
        request.setId(quoteId);
        request.setDiagnosisId(dto.getDiagnosisId());
        request.setClientName(dto.getClientName());
        request.setClientEmail(dto.getClientEmail());
        request.setClientPhone(dto.getClientPhone());
        request.setNotes(dto.getNotes());
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

        if (dto.getProducts() != null) {
            for (QuoteRequestDTO.ProductItemDTO itemDto : dto.getProducts()) {
                QuoteItem item = new QuoteItem(
                        itemDto.getId(),
                        itemDto.getName(),
                        itemDto.getDose(),
                        itemDto.getPrice()
                );
                request.addItem(item);
            }
        }

        return quoteRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<QuoteRequest> getAllQuotes() {
        return quoteRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<QuoteRequest> getQuoteById(String id) {
        return quoteRequestRepository.findById(id);
    }
}
