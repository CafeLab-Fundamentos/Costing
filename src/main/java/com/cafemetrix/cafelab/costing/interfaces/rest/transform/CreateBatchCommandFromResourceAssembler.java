package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.commands.CreateBatchCommand;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.CreateBatchResource;

public class CreateBatchCommandFromResourceAssembler {

    private CreateBatchCommandFromResourceAssembler() {}

    public static CreateBatchCommand toCommandFromResource(Long userId, CreateBatchResource resource) {
        return new CreateBatchCommand(
                userId,
                resource.batchName(),
                resource.registrationDate()
        );
    }
}
