package com.cafemetrix.cafelab.costing.domain.model.commands;

public record RegisterIndirectCostsCommand(
        Double transport,
        Integer storageDays,
        Double dailyStorageCost,
        Double electricity,
        Double machineryMaintenance,
        Double processingSupplies,
        Double waterUsed,
        Double equipmentDepreciation,
        Double qualityControl,
        Double certifications,
        Double insurance,
        Double adminExpenses
) {}
