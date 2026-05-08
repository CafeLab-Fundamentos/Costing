package com.cafemetrix.cafelab.costing.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductionTimeTest {

    @Test
    void shouldCreateValidProductionTime() {
        var time = new ProductionTime(60);
        assertEquals(60, time.minutes());
    }

    @Test
    void shouldAcceptOneMinuteAsMinimum() {
        var time = new ProductionTime(1);
        assertEquals(1, time.minutes());
    }

    @Test
    void shouldThrowWhenMinutesIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new ProductionTime(null));
    }

    @Test
    void shouldThrowWhenMinutesIsZero() {
        assertThrows(IllegalArgumentException.class, () -> new ProductionTime(0));
    }

    @Test
    void shouldThrowWhenMinutesIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new ProductionTime(-10));
    }
}
