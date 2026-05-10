package com.cafemetrix.cafelab.costing.application.internal.commandservices;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceCommandService;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.LotPerformanceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LotPerformanceCommandServiceImpl implements LotPerformanceCommandService {

    private final LotPerformanceRepository lotPerformanceRepository;

    public LotPerformanceCommandServiceImpl(LotPerformanceRepository lotPerformanceRepository) {
        this.lotPerformanceRepository = lotPerformanceRepository;
    }

    @Override
    public Optional<LotPerformance> handle(RegisterLotPerformanceCommand command) {
        var lotPerformance = new LotPerformance(command);
        var saved = lotPerformanceRepository.save(lotPerformance);
        saved.assignCoffeeLotIdFromAggregateId();
        lotPerformanceRepository.save(saved);
        return Optional.of(saved);
    }
}
