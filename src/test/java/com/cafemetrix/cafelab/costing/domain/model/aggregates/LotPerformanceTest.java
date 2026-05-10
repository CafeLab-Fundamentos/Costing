package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LotPerformanceTest {

    @Test
    void shouldCalculateYieldPercentageCorrectly() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60);
        var performance = new LotPerformance(command);
        assertEquals(85.0, performance.getYieldPercentage());
    }

    @Test
    void shouldCalculateLossWeightCorrectly() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60);
        var performance = new LotPerformance(command);
        assertEquals(15.0, performance.getLossWeight());
    }

    @Test
    void shouldCalculateProductivityPerHourCorrectly() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60);
        var performance = new LotPerformance(command);
        assertEquals(85.0, performance.calculateProductivityPerHour(), 0.01);
    }

    @Test
    void shouldExposeCoffeeLotIdAsAggregateId() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 90.0, 45);
        var performance = new LotPerformance(command);
        assertEquals(performance.getId(), performance.getCoffeeLotId());
    }

    @Test
    void shouldStoreUserIdFromCommand() {
        var command = new RegisterLotPerformanceCommand(77L, 100.0, 85.0, 60);
        var performance = new LotPerformance(command);
        assertEquals(77L, performance.getUserId());
    }

    @Test
    void shouldAcceptNullUserId() {
        var command = new RegisterLotPerformanceCommand(null, 100.0, 85.0, 60);
        var performance = new LotPerformance(command);
        assertNull(performance.getUserId());
    }

    @Test
    void shouldStoreProductionTimeMinutes() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 45);
        var performance = new LotPerformance(command);
        assertEquals(45, performance.getProductionTimeMinutes());
    }

    @Test
    void shouldRejectFinalWeightGreaterThanInitialWeight() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 105.0, 60);
        assertThrows(IllegalArgumentException.class, () -> new LotPerformance(command));
    }

    @Test
    void shouldAllowFinalWeightEqualToInitialWeight() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 100.0, 60);
        var performance = new LotPerformance(command);
        assertEquals(100.0, performance.getYieldPercentage());
        assertEquals(0.0, performance.getLossWeight());
    }

    @Test
    void shouldRoundYieldToTwoDecimalPlaces() {
        var command = new RegisterLotPerformanceCommand(1L, 120.0, 88.0, 30);
        var performance = new LotPerformance(command);
        assertEquals(73.33, performance.getYieldPercentage(), 0.001);
    }
}
