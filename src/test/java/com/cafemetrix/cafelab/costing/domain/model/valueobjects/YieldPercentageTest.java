package com.cafemetrix.cafelab.costing.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YieldPercentageTest {

    @Test
    void shouldCreateValidYieldPercentage() {
        var yield = new YieldPercentage(85.5);
        assertEquals(85.5, yield.value());
    }

    @Test
    void shouldAcceptZeroAsValidBoundary() {
        var yield = new YieldPercentage(0.0);
        assertEquals(0.0, yield.value());
    }

    @Test
    void shouldAcceptHundredAsValidBoundary() {
        var yield = new YieldPercentage(100.0);
        assertEquals(100.0, yield.value());
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new YieldPercentage(null));
    }

    @Test
    void shouldThrowWhenValueIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new YieldPercentage(-1.0));
    }

    @Test
    void shouldThrowWhenValueExceedsHundred() {
        assertThrows(IllegalArgumentException.class, () -> new YieldPercentage(101.0));
    }
}
