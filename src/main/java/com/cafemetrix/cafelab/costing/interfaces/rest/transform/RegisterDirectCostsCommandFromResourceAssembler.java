package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterDirectCostsResource;

public class RegisterDirectCostsCommandFromResourceAssembler {

    private RegisterDirectCostsCommandFromResourceAssembler() {}

    public static RegisterDirectCostsCommand toCommandFromResource(RegisterDirectCostsResource resource) {
        return new RegisterDirectCostsCommand(
                resource.coffeeLotId(),
                resource.rawMaterialCost(),
                resource.coffeeQuantityKg(),
                resource.hoursWorked(),
                resource.costPerHour(),
                resource.numWorkers()
        );
    }
}
