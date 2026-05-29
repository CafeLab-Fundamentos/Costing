package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.entities.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    void deleteByBatchId(Long batchId);
}
