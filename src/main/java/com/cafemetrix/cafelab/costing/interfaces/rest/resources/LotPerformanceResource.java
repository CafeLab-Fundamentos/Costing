package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record LotPerformanceResource(
        Long id,
        Long coffeeLotId,
        Double initialWeight,
        Double finalWeight,
        Double yieldPercentage,
        Double lossWeight,
        Integer productionTimeMinutes,
        Double productivityPerHour
) {}
