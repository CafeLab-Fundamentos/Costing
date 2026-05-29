package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterIndirectCostsCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinancialIndicatorsTest {

    private static CostSummary buildSummary() {
        var direct = new DirectCosts(10L,
                new RegisterDirectCostsCommand(1L, 20.0, 50.0, 8, 12.5, 3));
        var indirect = new IndirectCosts(10L,
                new RegisterIndirectCostsCommand(
                        100.0, 10, 5.0,
                        40.0, 30.0, 20.0, 10.0,
                        15.0, 25.0, 5.0, 12.0, 8.0));
        return new CostSummary(10L, direct, indirect, 18.0);
    }

    @Test
    void shouldComputeSuggestedPriceWithMarkup() {
        // costPerKg = 32.30, margin 25% => suggested = 32.30 * 1.25 = 40.375 -> 40.38
        var indicators = new FinancialIndicators(10L, buildSummary(), 25.0);
        assertEquals(32.30, indicators.getCostPerKg(), 0.01);
        assertEquals(25.0, indicators.getPotentialMargin(), 0.001);
        assertEquals(40.38, indicators.getSuggestedPrice(), 0.01);
    }

    @Test
    void shouldRejectNegativeMargin() {
        assertThrows(IllegalArgumentException.class,
                () -> new FinancialIndicators(10L, buildSummary(), -5.0));
    }
}
