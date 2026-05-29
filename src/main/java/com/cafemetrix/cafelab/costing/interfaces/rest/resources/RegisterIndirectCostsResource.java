package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RegisterIndirectCostsResource(
        @NotNull @PositiveOrZero Double transport,
        @NotNull @PositiveOrZero Integer storageDays,
        @NotNull @PositiveOrZero Double dailyStorageCost,
        @NotNull @PositiveOrZero Double electricity,
        @NotNull @PositiveOrZero Double machineryMaintenance,
        @NotNull @PositiveOrZero Double processingSupplies,
        @NotNull @PositiveOrZero Double waterUsed,
        @NotNull @PositiveOrZero Double equipmentDepreciation,
        @NotNull @PositiveOrZero Double qualityControl,
        @NotNull @PositiveOrZero Double certifications,
        @NotNull @PositiveOrZero Double insurance,
        @NotNull @PositiveOrZero Double adminExpenses
) {}
