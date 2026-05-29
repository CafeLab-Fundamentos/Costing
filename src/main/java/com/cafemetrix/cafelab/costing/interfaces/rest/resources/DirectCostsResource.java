package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record DirectCostsResource(
        Long id,
        Long batchId,
        Long coffeeLotId,
        Double rawMaterialCost,
        Double coffeeQuantityKg,
        Double totalRawMaterial,
        Integer hoursWorked,
        Double costPerHour,
        Integer numWorkers,
        Double totalLaborCost
) {}
