package com.cafemetrix.cafelab.costing.domain.model.commands;

import java.util.List;

public record CompareLotsPerformanceCommand(List<Long> coffeeLotIds) {}
