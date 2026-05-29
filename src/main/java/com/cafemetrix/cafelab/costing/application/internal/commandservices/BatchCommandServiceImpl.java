package com.cafemetrix.cafelab.costing.application.internal.commandservices;

import com.cafemetrix.cafelab.costing.domain.exceptions.BatchNotFoundException;
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
import com.cafemetrix.cafelab.costing.domain.services.BatchCommandService;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.BatchRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.CostSummaryRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.DirectCostsRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.FinancialIndicatorsRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.IndirectCostsRepository;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BatchCommandServiceImpl implements BatchCommandService {

    private final BatchRepository batchRepository;
    private final DirectCostsRepository directCostsRepository;
    private final IndirectCostsRepository indirectCostsRepository;
    private final CostSummaryRepository costSummaryRepository;
    private final FinancialIndicatorsRepository financialIndicatorsRepository;
    private final RecommendationRepository recommendationRepository;

    public BatchCommandServiceImpl(BatchRepository batchRepository,
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
    @Transactional
    public Optional<Batch> handle(CreateBatchCommand command) {
        var batch = new Batch(command);
        return Optional.of(batchRepository.save(batch));
    }

    @Override
    @Transactional
    public Optional<Batch> handle(UpdateBatchCommand command) {
        var batch = batchRepository.findById(command.batchId())
                .orElseThrow(() -> new BatchNotFoundException(command.batchId()));
        batch.rename(command.batchName());
        return Optional.of(batchRepository.save(batch));
    }

    @Override
    @Transactional
    public void deleteBatch(Long batchId) {
        if (!batchRepository.existsById(batchId)) {
            throw new BatchNotFoundException(batchId);
        }
        recommendationRepository.deleteByBatchId(batchId);
        financialIndicatorsRepository.deleteByBatchId(batchId);
        costSummaryRepository.deleteByBatchId(batchId);
        indirectCostsRepository.deleteByBatchId(batchId);
        directCostsRepository.deleteByBatchId(batchId);
        batchRepository.deleteById(batchId);
    }

    @Override
    @Transactional
    public DirectCosts handleDirectCosts(Long batchId, RegisterDirectCostsCommand command) {
        ensureBatchExists(batchId);
        var existing = directCostsRepository.findByBatchId(batchId);
        if (existing.isPresent()) {
            var entity = existing.get();
            entity.applyUpdate(command);
            return directCostsRepository.save(entity);
        }
        return directCostsRepository.save(new DirectCosts(batchId, command));
    }

    @Override
    @Transactional
    public IndirectCosts handleIndirectCosts(Long batchId, RegisterIndirectCostsCommand command) {
        ensureBatchExists(batchId);
        var existing = indirectCostsRepository.findByBatchId(batchId);
        if (existing.isPresent()) {
            var entity = existing.get();
            entity.applyUpdate(command);
            return indirectCostsRepository.save(entity);
        }
        return indirectCostsRepository.save(new IndirectCosts(batchId, command));
    }

    @Override
    @Transactional
    public ComputedCostingResult handle(ComputeBatchCostingCommand command) {
        ensureBatchExists(command.batchId());
        var direct = directCostsRepository.findByBatchId(command.batchId())
                .orElseThrow(() -> new IllegalStateException(
                        "DirectCosts must be registered before computing the batch costing"));
        var indirect = indirectCostsRepository.findByBatchId(command.batchId())
                .orElseThrow(() -> new IllegalStateException(
                        "IndirectCosts must be registered before computing the batch costing"));

        var summary = costSummaryRepository.findByBatchId(command.batchId())
                .map(s -> {
                    s.recompute(direct, indirect, command.gramsPerCup());
                    return s;
                })
                .orElseGet(() -> new CostSummary(command.batchId(), direct, indirect, command.gramsPerCup()));
        summary = costSummaryRepository.save(summary);

        CostSummary persistedSummary = summary;
        var indicators = financialIndicatorsRepository.findByBatchId(command.batchId())
                .map(i -> {
                    i.recompute(persistedSummary, command.targetMarginPercentage());
                    return i;
                })
                .orElseGet(() -> new FinancialIndicators(command.batchId(), persistedSummary,
                        command.targetMarginPercentage()));
        indicators = financialIndicatorsRepository.save(indicators);

        return new ComputedCostingResult(summary, indicators);
    }

    @Override
    @Transactional
    public Recommendation handle(Long batchId, AddRecommendationCommand command) {
        ensureBatchExists(batchId);
        return recommendationRepository.save(new Recommendation(batchId, command));
    }

    @Override
    @Transactional
    public void handle(DeleteRecommendationCommand command) {
        if (!recommendationRepository.existsById(command.recommendationId())) {
            throw new IllegalArgumentException(
                    "Recommendation with id " + command.recommendationId() + " was not found");
        }
        recommendationRepository.deleteById(command.recommendationId());
    }

    private void ensureBatchExists(Long batchId) {
        if (!batchRepository.existsById(batchId)) {
            throw new BatchNotFoundException(batchId);
        }
    }
}
