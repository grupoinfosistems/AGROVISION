package com.agrovision.repository;

import com.agrovision.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, String> {

    List<Diagnosis> findByClientEmailOrderByCreatedAtDesc(String clientEmail);

    List<Diagnosis> findAllByOrderByCreatedAtDesc();
}
