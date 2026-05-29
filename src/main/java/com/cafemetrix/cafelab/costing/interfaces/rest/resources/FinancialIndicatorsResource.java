package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record FinancialIndicatorsResource(
        Long id,
        Long batchId,
        Double costPerKg,
        Double potentialMargin,
        Double suggestedPrice
) {}
