package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record RecommendationResource(
        Long id,
        Long batchId,
        String recommendationText
) {}
