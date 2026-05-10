package com.cafemetrix.cafelab.costing.interfaces.acl;

public interface CostingContextFacade {

    /**
     * Registers performance metrics for a roasted coffee lot.
     *
     * @param userId optional application user associated with the record
     * @return the internal ID of the created LotPerformance, or null on failure
     */
    Long registerLotPerformance(Long userId, Double initialWeight,
                                Double finalWeight, Integer productionTimeMinutes);

    /**
     * Returns the yield percentage for the given performance record id, or null if not found.
     */
    Double fetchYieldPercentageByCoffeeLotId(Long coffeeLotId);

    /**
     * Returns true if a performance record exists for the given id.
     */
    boolean hasPerformanceRegistered(Long coffeeLotId);
}
