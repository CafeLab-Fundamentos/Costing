package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LotPerformanceTest {

    private static RegisterLotPerformanceCommand cmd(Long coffeeLotId, Double initial, Double finalW, Integer minutes) {
        return new RegisterLotPerformanceCommand(99L, coffeeLotId, initial, finalW, minutes);
    }

    @Test
    void shouldCalculateYieldPercentageCorrectly() {
        var performance = new LotPerformance(cmd(1L, 100.0, 85.0, 60));
        assertEquals(85.0, performance.getYieldPercentage());
    }

    @Test
    void shouldCalculateLossWeightCorrectly() {
        var performance = new LotPerformance(cmd(1L, 100.0, 85.0, 60));
        assertEquals(15.0, performance.getLossWeight());
    }

    @Test
    void shouldExposeLossPercentageAsComplementOfYield() {
        // US14: % de merma = 100 - yield
        var performance = new LotPerformance(cmd(1L, 100.0, 85.0, 60));
        assertEquals(15.0, performance.getLossPercentage(), 0.001);
    }

    @Test
    void shouldCalculateProductivityPerHourCorrectly() {
        // 85 kg / 60 min * 60 = 85 kg/h
        var performance = new LotPerformance(cmd(1L, 100.0, 85.0, 60));
        assertEquals(85.0, performance.calculateProductivityPerHour(), 0.01);
    }

    @Test
    void shouldStoreUserId() {
        var performance = new LotPerformance(new RegisterLotPerformanceCommand(7L, 1L, 100.0, 85.0, 60));
        assertEquals(7L, performance.getUserId());
    }

    @Test
    void shouldReturnCorrectCoffeeLotId() {
        var performance = new LotPerformance(cmd(42L, 100.0, 90.0, 45));
        assertEquals(42L, performance.getCoffeeLotId());
    }

    @Test
    void shouldStoreProductionTimeMinutes() {
        var performance = new LotPerformance(cmd(1L, 100.0, 85.0, 45));
        assertEquals(45, performance.getProductionTimeMinutes());
    }

    @Test
    void shouldRejectFinalWeightGreaterThanInitialWeight() {
        var command = cmd(1L, 100.0, 105.0, 60);
        assertThrows(IllegalArgumentException.class, () -> new LotPerformance(command));
    }

    @Test
    void shouldAllowFinalWeightEqualToInitialWeight() {
        var performance = new LotPerformance(cmd(1L, 100.0, 100.0, 60));
        assertEquals(100.0, performance.getYieldPercentage());
        assertEquals(0.0, performance.getLossWeight());
        assertEquals(0.0, performance.getLossPercentage());
    }

    @Test
    void shouldRoundYieldToTwoDecimalPlaces() {
        // 88 / 120 * 100 = 73.333...  -> 73.33
        var performance = new LotPerformance(cmd(1L, 120.0, 88.0, 30));
        assertEquals(73.33, performance.getYieldPercentage(), 0.001);
        assertEquals(26.67, performance.getLossPercentage(), 0.001);
    }
}
