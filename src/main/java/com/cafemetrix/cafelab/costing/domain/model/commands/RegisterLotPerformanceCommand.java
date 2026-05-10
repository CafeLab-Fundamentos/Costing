package com.cafemetrix.cafelab.costing.domain.model.commands;

public record RegisterLotPerformanceCommand(
        Long userId,
        Double initialWeight,
        Double finalWeight,
        Integer productionTimeMinutes
) {}
