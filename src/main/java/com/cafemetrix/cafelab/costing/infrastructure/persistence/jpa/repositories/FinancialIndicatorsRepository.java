package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.entities.FinancialIndicators;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialIndicatorsRepository extends JpaRepository<FinancialIndicators, Long> {
    Optional<FinancialIndicators> findByBatchId(Long batchId);

    void deleteByBatchId(Long batchId);
}
