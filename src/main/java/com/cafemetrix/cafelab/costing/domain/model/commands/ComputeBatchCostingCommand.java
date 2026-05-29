package com.cafemetrix.cafelab.costing.domain.model.commands;

public record ComputeBatchCostingCommand(
        Long batchId,
        Double gramsPerCup,
        Double targetMarginPercentage
) {}
