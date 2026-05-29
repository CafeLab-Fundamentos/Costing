package com.cafemetrix.cafelab.costing.domain.model.commands;

public record UpdateBatchCommand(
        Long batchId,
        String batchName
) {}
