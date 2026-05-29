package com.cafemetrix.cafelab.costing.domain.services;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.Batch;
import com.cafemetrix.cafelab.costing.domain.model.commands.AddRecommendationCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.ComputeBatchCostingCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.CreateBatchCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.DeleteRecommendationCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterIndirectCostsCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.UpdateBatchCommand;
import com.cafemetrix.cafelab.costing.domain.model.entities.CostSummary;
import com.cafemetrix.cafelab.costing.domain.model.entities.DirectCosts;
import com.cafemetrix.cafelab.costing.domain.model.entities.FinancialIndicators;
import com.cafemetrix.cafelab.costing.domain.model.entities.IndirectCosts;
import com.cafemetrix.cafelab.costing.domain.model.entities.Recommendation;

import java.util.Optional;

public interface BatchCommandService {
    Optional<Batch> handle(CreateBatchCommand command);

    Optional<Batch> handle(UpdateBatchCommand command);

    void deleteBatch(Long batchId);

    DirectCosts handleDirectCosts(Long batchId, RegisterDirectCostsCommand command);

    IndirectCosts handleIndirectCosts(Long batchId, RegisterIndirectCostsCommand command);

    ComputedCostingResult handle(ComputeBatchCostingCommand command);

    Recommendation handle(Long batchId, AddRecommendationCommand command);

    void handle(DeleteRecommendationCommand command);

    /** Tupla con los dos artefactos calculados al recomputar un Batch. */
    record ComputedCostingResult(CostSummary costSummary, FinancialIndicators financialIndicators) {}
}
