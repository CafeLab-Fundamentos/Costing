package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record RegisterLotPerformanceResource(
        Long coffeeLotId,
        Double initialWeight,
        Double finalWeight,
        Integer productionTimeMinutes
) {}
