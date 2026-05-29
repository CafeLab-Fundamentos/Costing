package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.entities.FinancialIndicators;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.FinancialIndicatorsResource;

public class FinancialIndicatorsResourceFromEntityAssembler {

    private FinancialIndicatorsResourceFromEntityAssembler() {}

    public static FinancialIndicatorsResource toResourceFromEntity(FinancialIndicators entity) {
        return new FinancialIndicatorsResource(
                entity.getId(),
                entity.getBatchId(),
                entity.getCostPerKg(),
                entity.getPotentialMargin(),
                entity.getSuggestedPrice()
        );
    }
}
