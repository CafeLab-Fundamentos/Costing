package com.cafemetrix.cafelab.costing.interfaces.acl;

public interface CostingContextFacade {

    /**
     * Registers performance metrics for a roasted coffee lot.
     * @return the internal ID of the created LotPerformance, or null on failure
     */
    Long registerLotPerformance(Long coffeeLotId, Double initialWeight,
                                Double finalWeight, Integer productionTimeMinutes);

    /**
     * Returns the yield percentage for the given coffee lot, or null if not registered.
     */
    Double fetchYieldPercentageByCoffeeLotId(Long coffeeLotId);

    /**
     * Returns true if performance has already been registered for the given lot.
     */
    boolean hasPerformanceRegistered(Long coffeeLotId);
}
