package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RegisterDirectCostsResource(
        @NotNull Long coffeeLotId,
        @NotNull @PositiveOrZero Double rawMaterialCost,
        @NotNull @Positive Double coffeeQuantityKg,
        @NotNull @PositiveOrZero Integer hoursWorked,
        @NotNull @PositiveOrZero Double costPerHour,
        @NotNull @PositiveOrZero Integer numWorkers
) {}
