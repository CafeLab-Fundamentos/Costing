package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record IndirectCostsResource(
        Long id,
        Long batchId,
        Double transport,
        Integer storageDays,
        Double dailyStorageCost,
        Double totalStorageCost,
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
