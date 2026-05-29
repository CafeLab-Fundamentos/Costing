package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterIndirectCostsCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndirectCostsTest {

    private static RegisterIndirectCostsCommand cmd() {
        return new RegisterIndirectCostsCommand(
                100.0, 10, 5.0,
                40.0, 30.0, 20.0, 10.0,
                15.0, 25.0, 5.0, 12.0, 8.0);
    }

    @Test
    void shouldComputeTotalStorageCost() {
        var indirect = new IndirectCosts(10L, cmd());
        assertEquals(50.0, indirect.getTotalStorageCost(), 0.001);
    }

    @Test
    void shouldRejectNegativeFields() {
        var bad = new RegisterIndirectCostsCommand(
                -1.0, 10, 5.0,
                40.0, 30.0, 20.0, 10.0,
                15.0, 25.0, 5.0, 12.0, 8.0);
        assertThrows(IllegalArgumentException.class, () -> new IndirectCosts(10L, bad));
    }
}
