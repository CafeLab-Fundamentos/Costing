package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.entities.DirectCosts;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.DirectCostsResource;

public class DirectCostsResourceFromEntityAssembler {

    private DirectCostsResourceFromEntityAssembler() {}

    public static DirectCostsResource toResourceFromEntity(DirectCosts entity) {
        return new DirectCostsResource(
                entity.getId(),
                entity.getBatchId(),
                entity.getCoffeeLotId(),
                entity.getRawMaterialCost(),
                entity.getCoffeeQuantityKg(),
                entity.getTotalRawMaterial(),
                entity.getHoursWorked(),
                entity.getCostPerHour(),
                entity.getNumWorkers(),
                entity.getTotalLaborCost()
        );
    }
}
