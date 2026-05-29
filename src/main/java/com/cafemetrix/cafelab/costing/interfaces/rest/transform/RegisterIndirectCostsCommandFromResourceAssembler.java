package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterIndirectCostsCommand;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterIndirectCostsResource;

public class RegisterIndirectCostsCommandFromResourceAssembler {

    private RegisterIndirectCostsCommandFromResourceAssembler() {}

    public static RegisterIndirectCostsCommand toCommandFromResource(RegisterIndirectCostsResource resource) {
        return new RegisterIndirectCostsCommand(
                resource.transport(),
                resource.storageDays(),
                resource.dailyStorageCost(),
                resource.electricity(),
                resource.machineryMaintenance(),
                resource.processingSupplies(),
                resource.waterUsed(),
                resource.equipmentDepreciation(),
                resource.qualityControl(),
                resource.certifications(),
                resource.insurance(),
                resource.adminExpenses()
        );
    }
}
