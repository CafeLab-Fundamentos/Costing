package com.cafemetrix.cafelab.costing.domain.model.commands;

public record RegisterLotPerformanceCommand(
        Long userId,
        Long coffeeLotId,
        Double initialWeight,
        Double finalWeight,
        Integer productionTimeMinutes
) {}
