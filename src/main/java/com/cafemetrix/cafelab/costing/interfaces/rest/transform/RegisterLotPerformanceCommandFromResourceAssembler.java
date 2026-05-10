package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterLotPerformanceResource;

public class RegisterLotPerformanceCommandFromResourceAssembler {

    public static RegisterLotPerformanceCommand toCommandFromResource(RegisterLotPerformanceResource resource) {
        return new RegisterLotPerformanceCommand(
                resource.userId(),
                resource.initialWeight(),
                resource.finalWeight(),
                resource.productionTimeMinutes()
        );
    }
}
