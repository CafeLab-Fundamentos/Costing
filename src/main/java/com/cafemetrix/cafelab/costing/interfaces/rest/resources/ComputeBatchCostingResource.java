package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ComputeBatchCostingResource(
        @NotNull @Positive Double gramsPerCup,
        @NotNull @PositiveOrZero Double targetMarginPercentage
) {}
