package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.entities.CostSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CostSummaryRepository extends JpaRepository<CostSummary, Long> {
    Optional<CostSummary> findByBatchId(Long batchId);

    void deleteByBatchId(Long batchId);
}
