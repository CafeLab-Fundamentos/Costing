package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.entities.DirectCosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectCostsRepository extends JpaRepository<DirectCosts, Long> {
    Optional<DirectCosts> findByBatchId(Long batchId);

    void deleteByBatchId(Long batchId);
}
