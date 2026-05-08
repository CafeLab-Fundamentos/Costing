package com.cafemetrix.cafelab.costing.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record LossWeight(Double value) {
    public LossWeight() {
        this(null);
    }

    public LossWeight {
        if (value == null) {
            throw new IllegalArgumentException("Loss weight cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Loss weight cannot be negative");
        }
    }
}
