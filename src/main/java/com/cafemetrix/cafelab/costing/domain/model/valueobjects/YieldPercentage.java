package com.cafemetrix.cafelab.costing.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record YieldPercentage(Double value) {
    public YieldPercentage() {
        this(null);
    }

    public YieldPercentage {
        if (value == null) {
            throw new IllegalArgumentException("Yield percentage cannot be null");
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Yield percentage must be between 0 and 100");
        }
    }
}