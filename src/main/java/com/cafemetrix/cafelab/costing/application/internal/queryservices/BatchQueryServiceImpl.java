package com.cafemetrix.cafelab.costing.application.internal.queryservices;

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
import com.cafemetrix.cafelab.costing.domain.services.BatchQueryService;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.BatchRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.CostSummaryRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.DirectCostsRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.FinancialIndicatorsRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.IndirectCostsRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BatchQueryServiceImpl implements BatchQueryService {

    private final BatchRepository batchRepository;
    private final DirectCostsRepository directCostsRepository;
    private final IndirectCostsRepository indirectCostsRepository;
    private final CostSummaryRepository costSummaryRepository;
    private final FinancialIndicatorsRepository financialIndicatorsRepository;
    private final RecommendationRepository recommendationRepository;

    public BatchQueryServiceImpl(BatchRepository batchRepository,
                                 DirectCostsRepository directCostsRepository,
                                 IndirectCostsRepository indirectCostsRepository,
                                 CostSummaryRepository costSummaryRepository,
                                 FinancialIndicatorsRepository financialIndicatorsRepository,
                                 RecommendationRepository recommendationRepository) {
        this.batchRepository = batchRepository;
        this.directCostsRepository = directCostsRepository;
        this.indirectCostsRepository = indirectCostsRepository;
        this.costSummaryRepository = costSummaryRepository;
        this.financialIndicatorsRepository = financialIndicatorsRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public Optional<Batch> handle(GetBatchByIdQuery query) {
        return batchRepository.findById(query.id());
    }

    @Override
    public List<Batch> handle(GetBatchesByUserIdQuery query) {
        return batchRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<DirectCosts> handle(GetDirectCostsByBatchIdQuery query) {
        return directCostsRepository.findByBatchId(query.batchId());
    }

    @Override
    public Optional<IndirectCosts> handle(GetIndirectCostsByBatchIdQuery query) {
        return indirectCostsRepository.findByBatchId(query.batchId());
    }

    @Override
    public Optional<CostSummary> handle(GetCostSummaryByBatchIdQuery query) {
        return costSummaryRepository.findByBatchId(query.batchId());
    }

    @Override
    public Optional<FinancialIndicators> handle(GetFinancialIndicatorsByBatchIdQuery query) {
        return financialIndicatorsRepository.findByBatchId(query.batchId());
    }

    @Override
    public List<Recommendation> handle(GetRecommendationsByBatchIdQuery query) {
        return recommendationRepository.findByBatchIdOrderByCreatedAtDesc(query.batchId());
    }
}
