package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterLotPerformanceResource;

public class RegisterLotPerformanceCommandFromResourceAssembler {

    private RegisterLotPerformanceCommandFromResourceAssembler() {}

    public static RegisterLotPerformanceCommand toCommandFromResource(Long userId,
                                                                      RegisterLotPerformanceResource resource) {
        return new RegisterLotPerformanceCommand(
                userId,
                resource.coffeeLotId(),
                resource.initialWeight(),
                resource.finalWeight(),
                resource.productionTimeMinutes()
        );
    }
}
