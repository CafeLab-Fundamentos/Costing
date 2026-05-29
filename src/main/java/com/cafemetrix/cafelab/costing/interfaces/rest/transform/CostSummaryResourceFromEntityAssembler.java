package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.entities.CostSummary;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.CostSummaryResource;

public class CostSummaryResourceFromEntityAssembler {

    private CostSummaryResourceFromEntityAssembler() {}

    public static CostSummaryResource toResourceFromEntity(CostSummary entity) {
        return new CostSummaryResource(
                entity.getId(),
                entity.getBatchId(),
                entity.getRawMaterial(),
                entity.getDirectLabor(),
                entity.getTransport(),
                entity.getStorage(),
                entity.getProcessing(),
                entity.getOtherCosts(),
                entity.getTotal(),
                entity.getCostPerKg(),
                entity.getCostPerCup()
        );
    }
}
