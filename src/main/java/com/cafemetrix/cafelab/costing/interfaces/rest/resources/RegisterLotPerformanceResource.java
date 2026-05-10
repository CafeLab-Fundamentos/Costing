package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record RegisterLotPerformanceResource(
        Long userId,
        Double initialWeight,
        Double finalWeight,
        Integer productionTimeMinutes
) {}
