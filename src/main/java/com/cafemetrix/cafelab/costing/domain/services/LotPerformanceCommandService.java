package com.cafemetrix.cafelab.costing.domain.services;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;

import java.util.Optional;

public interface LotPerformanceCommandService {
    Optional<LotPerformance> handle(RegisterLotPerformanceCommand command);
}
