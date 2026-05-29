package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record AddRecommendationResource(
        @NotBlank String recommendationText
) {}
