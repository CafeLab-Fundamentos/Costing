package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.entities.IndirectCosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndirectCostsRepository extends JpaRepository<IndirectCosts, Long> {
    Optional<IndirectCosts> findByBatchId(Long batchId);

    void deleteByBatchId(Long batchId);
}
