package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.LotPerformanceResource;

public class LotPerformanceResourceFromEntityAssembler {

    public static LotPerformanceResource toResourceFromEntity(LotPerformance entity) {
        return new LotPerformanceResource(
                entity.getId(),
                entity.getCoffeeLotId(),
                entity.getInitialWeight(),
                entity.getFinalWeight(),
                entity.getYieldPercentage(),
                entity.getLossWeight(),
                entity.getProductionTimeMinutes(),
                entity.calculateProductivityPerHour()
        );
    }
}
