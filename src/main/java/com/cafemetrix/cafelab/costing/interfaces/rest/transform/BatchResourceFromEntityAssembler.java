package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.Batch;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.BatchResource;

public class BatchResourceFromEntityAssembler {

    private BatchResourceFromEntityAssembler() {}

    public static BatchResource toResourceFromEntity(Batch entity) {
        return new BatchResource(
                entity.getId(),
                entity.getUserId(),
                entity.getBatchName(),
                entity.getRegistrationDate()
        );
    }
}
