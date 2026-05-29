package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectCostsTest {

    private static RegisterDirectCostsCommand cmd() {
        return new RegisterDirectCostsCommand(1L, 20.0, 50.0, 8, 12.5, 3);
    }

    @Test
    void shouldComputeTotalRawMaterial() {
        var direct = new DirectCosts(10L, cmd());
        assertEquals(1000.0, direct.getTotalRawMaterial(), 0.001);
    }

    @Test
    void shouldComputeTotalLaborCost() {
        // hoursWorked=8, costPerHour=12.5, numWorkers=3 => 8 * 12.5 * 3 = 300
        var direct = new DirectCosts(10L, cmd());
        assertEquals(300.0, direct.getTotalLaborCost(), 0.001);
    }

    @Test
    void shouldRejectZeroOrNegativeCoffeeQuantity() {
        var command = new RegisterDirectCostsCommand(1L, 20.0, 0.0, 8, 12.5, 3);
        assertThrows(IllegalArgumentException.class, () -> new DirectCosts(10L, command));
    }

    @Test
    void shouldRecomputeOnUpdate() {
        var direct = new DirectCosts(10L, cmd());
        direct.applyUpdate(new RegisterDirectCostsCommand(1L, 30.0, 10.0, 4, 10.0, 2));
        assertEquals(300.0, direct.getTotalRawMaterial(), 0.001);
        assertEquals(80.0, direct.getTotalLaborCost(), 0.001);
    }
}
