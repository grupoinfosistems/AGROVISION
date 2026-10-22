package com.agrovision.repository;

import com.agrovision.model.QuoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, String> {

    List<QuoteRequest> findByClientEmailOrderByCreatedAtDesc(String clientEmail);

    List<QuoteRequest> findAllByOrderByCreatedAtDesc();
}
