package com.cafemetrix.cafelab.costing.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ProductionTime(Integer minutes) {
    public ProductionTime() {
        this(null);
    }

    public ProductionTime {
        if (minutes == null) {
            throw new IllegalArgumentException("Production time cannot be null");
        }
        if (minutes <= 0) {
            throw new IllegalArgumentException("Production time must be greater than zero");
        }
    }
}
