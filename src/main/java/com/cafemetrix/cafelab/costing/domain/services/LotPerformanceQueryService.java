package com.cafemetrix.cafelab.costing.domain.services;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetPerformanceComparisonQuery;

import java.util.List;
import java.util.Optional;

public interface LotPerformanceQueryService {
    Optional<LotPerformance> handle(GetLotPerformanceByIdQuery query);
    Optional<LotPerformance> handle(GetLotPerformanceByCoffeeLotIdQuery query);
    List<LotPerformance> handle(GetAllLotPerformancesQuery query);
    List<LotPerformance> handle(GetPerformanceComparisonQuery query);
}
