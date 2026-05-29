package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterIndirectCostsCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CostSummaryTest {

    private static DirectCosts direct() {
        return new DirectCosts(10L,
                new RegisterDirectCostsCommand(1L, 20.0, 50.0, 8, 12.5, 3));
    }

    private static IndirectCosts indirect() {
        return new IndirectCosts(10L,
                new RegisterIndirectCostsCommand(
                        100.0, 10, 5.0,
                        40.0, 30.0, 20.0, 10.0,
                        15.0, 25.0, 5.0, 12.0, 8.0));
    }

    @Test
    void shouldAggregateAllCostBucketsAndDerivePerKgAndPerCup() {
        var summary = new CostSummary(10L, direct(), indirect(), 18.0);
        // raw material 1000, direct labor 300, transport 100, storage 50
        // processing = 40+30+20+10 = 100
        // other = 15+25+5+12+8 = 65
        // total = 1615
        // costPerKg = 1615 / 50 = 32.30
        // costPerCup = 32.30 * 0.018 = 0.5814 -> 0.58
        assertEquals(1000.0, summary.getRawMaterial(), 0.001);
        assertEquals(300.0, summary.getDirectLabor(), 0.001);
        assertEquals(100.0, summary.getTransport(), 0.001);
        assertEquals(50.0, summary.getStorage(), 0.001);
        assertEquals(100.0, summary.getProcessing(), 0.001);
        assertEquals(65.0, summary.getOtherCosts(), 0.001);
        assertEquals(1615.0, summary.getTotal(), 0.001);
        assertEquals(32.30, summary.getCostPerKg(), 0.01);
        assertEquals(0.58, summary.getCostPerCup(), 0.01);
    }

    @Test
    void shouldRejectNonPositiveGramsPerCup() {
        assertThrows(IllegalArgumentException.class, () -> new CostSummary(10L, direct(), indirect(), 0.0));
    }
}
