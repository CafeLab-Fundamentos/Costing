package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record CostSummaryResource(
        Long id,
        Long batchId,
        Double rawMaterial,
        Double directLabor,
        Double transport,
        Double storage,
        Double processing,
        Double otherCosts,
        Double total,
        Double costPerKg,
        Double costPerCup
) {}
