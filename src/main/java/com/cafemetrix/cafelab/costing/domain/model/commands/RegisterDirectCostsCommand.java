package com.cafemetrix.cafelab.costing.domain.model.commands;

public record RegisterDirectCostsCommand(
        Long coffeeLotId,
        Double rawMaterialCost,
        Double coffeeQuantityKg,
        Integer hoursWorked,
        Double costPerHour,
        Integer numWorkers
) {}
