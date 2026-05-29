package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByUserId(Long userId);
}
