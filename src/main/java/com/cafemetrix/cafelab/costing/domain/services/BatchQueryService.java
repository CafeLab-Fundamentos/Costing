package com.cafemetrix.cafelab.costing.domain.services;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.Batch;
import com.cafemetrix.cafelab.costing.domain.model.entities.CostSummary;
import com.cafemetrix.cafelab.costing.domain.model.entities.DirectCosts;
import com.cafemetrix.cafelab.costing.domain.model.entities.FinancialIndicators;
import com.cafemetrix.cafelab.costing.domain.model.entities.IndirectCosts;
import com.cafemetrix.cafelab.costing.domain.model.entities.Recommendation;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetBatchByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetBatchesByUserIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetCostSummaryByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetDirectCostsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetFinancialIndicatorsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetIndirectCostsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetRecommendationsByBatchIdQuery;

import java.util.List;
import java.util.Optional;

public interface BatchQueryService {
    Optional<Batch> handle(GetBatchByIdQuery query);

    List<Batch> handle(GetBatchesByUserIdQuery query);

    Optional<DirectCosts> handle(GetDirectCostsByBatchIdQuery query);

    Optional<IndirectCosts> handle(GetIndirectCostsByBatchIdQuery query);

    Optional<CostSummary> handle(GetCostSummaryByBatchIdQuery query);

    Optional<FinancialIndicators> handle(GetFinancialIndicatorsByBatchIdQuery query);

    List<Recommendation> handle(GetRecommendationsByBatchIdQuery query);
}
