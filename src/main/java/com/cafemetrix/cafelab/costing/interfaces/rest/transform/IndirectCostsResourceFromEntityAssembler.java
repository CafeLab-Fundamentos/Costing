package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.entities.IndirectCosts;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.IndirectCostsResource;

public class IndirectCostsResourceFromEntityAssembler {

    private IndirectCostsResourceFromEntityAssembler() {}

    public static IndirectCostsResource toResourceFromEntity(IndirectCosts entity) {
        return new IndirectCostsResource(
                entity.getId(),
                entity.getBatchId(),
                entity.getTransport(),
                entity.getStorageDays(),
                entity.getDailyStorageCost(),
                entity.getTotalStorageCost(),
                entity.getElectricity(),
                entity.getMachineryMaintenance(),
                entity.getProcessingSupplies(),
                entity.getWaterUsed(),
                entity.getEquipmentDepreciation(),
                entity.getQualityControl(),
                entity.getCertifications(),
                entity.getInsurance(),
                entity.getAdminExpenses()
        );
    }
}
