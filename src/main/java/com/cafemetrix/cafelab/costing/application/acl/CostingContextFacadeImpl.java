package com.cafemetrix.cafelab.costing.application.acl;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceCommandService;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceQueryService;
import com.cafemetrix.cafelab.costing.interfaces.acl.CostingContextFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CostingContextFacadeImpl implements CostingContextFacade {

    private static final Logger log = LoggerFactory.getLogger(CostingContextFacadeImpl.class);

    private final LotPerformanceCommandService commandService;
    private final LotPerformanceQueryService queryService;

    public CostingContextFacadeImpl(LotPerformanceCommandService commandService,
                                    LotPerformanceQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @Override
    public Long registerLotPerformance(Long userId, Double initialWeight,
                                       Double finalWeight, Integer productionTimeMinutes) {
        try {
            var command = new RegisterLotPerformanceCommand(userId, initialWeight, finalWeight, productionTimeMinutes);
            return commandService.handle(command)
                    .map(p -> p.getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to register lot performance: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Double fetchYieldPercentageByCoffeeLotId(Long coffeeLotId) {
        return queryService.handle(new GetLotPerformanceByCoffeeLotIdQuery(coffeeLotId))
                .map(p -> p.getYieldPercentage())
                .orElse(null);
    }

    @Override
    public boolean hasPerformanceRegistered(Long coffeeLotId) {
        return queryService.handle(new GetLotPerformanceByCoffeeLotIdQuery(coffeeLotId)).isPresent();
    }
}
