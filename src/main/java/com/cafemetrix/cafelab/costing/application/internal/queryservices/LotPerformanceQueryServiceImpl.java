package com.cafemetrix.cafelab.costing.application.internal.queryservices;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetPerformanceComparisonQuery;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceQueryService;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.LotPerformanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LotPerformanceQueryServiceImpl implements LotPerformanceQueryService {

    private final LotPerformanceRepository lotPerformanceRepository;

    public LotPerformanceQueryServiceImpl(LotPerformanceRepository lotPerformanceRepository) {
        this.lotPerformanceRepository = lotPerformanceRepository;
    }

    @Override
    public Optional<LotPerformance> handle(GetLotPerformanceByIdQuery query) {
        return lotPerformanceRepository.findById(query.id());
    }

    @Override
    public Optional<LotPerformance> handle(GetLotPerformanceByCoffeeLotIdQuery query) {
        return lotPerformanceRepository.findFirstByCoffeeLotIdOrderByIdDesc(query.coffeeLotId());
    }

    @Override
    public List<LotPerformance> handle(GetAllLotPerformancesQuery query) {
        return lotPerformanceRepository.findAll();
    }

    @Override
    public List<LotPerformance> handle(GetAllLotPerformancesByCoffeeLotIdQuery query) {
        return lotPerformanceRepository.findByCoffeeLotId(query.coffeeLotId());
    }

    @Override
    public List<LotPerformance> handle(GetPerformanceComparisonQuery query) {
        if (query.coffeeLotIds() == null || query.coffeeLotIds().size() < 2) {
            throw new IllegalArgumentException("At least two coffee lot IDs are required for comparison");
        }
        return lotPerformanceRepository.findAllById(query.coffeeLotIds());
    }
}
