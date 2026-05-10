package com.cafemetrix.cafelab.costing.domain.model.queries;

/**
 * @param coffeeLotId value persisted in {@code coffee_lot_id}, aligned with the aggregate {@code id}.
 */
public record GetAllLotPerformancesByCoffeeLotIdQuery(Long coffeeLotId) {}
