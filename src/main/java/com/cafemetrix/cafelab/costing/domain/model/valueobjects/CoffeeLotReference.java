package com.cafemetrix.cafelab.costing.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record CoffeeLotReference(Long value) {
    public CoffeeLotReference() {
        this(null);
    }

    public CoffeeLotReference {
        if (value == null) {
            throw new IllegalArgumentException("Coffee lot ID cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Coffee lot ID must be a positive number");
        }
    }
}
