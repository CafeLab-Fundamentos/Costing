package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

public record BatchCostingReportResource(
        CostSummaryResource costSummary,
        FinancialIndicatorsResource financialIndicators
) {}
