package com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LotPerformanceRepository extends JpaRepository<LotPerformance, Long> {

    List<LotPerformance> findByCoffeeLotId(Long coffeeLotId);

    Optional<LotPerformance> findFirstByCoffeeLotIdOrderByIdDesc(Long coffeeLotId);

    List<LotPerformance> findByUserId(Long userId);
}
