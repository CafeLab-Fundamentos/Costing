package com.cafemetrix.cafelab.costing.domain.exceptions;

public class LotPerformanceNotFoundException extends RuntimeException {
    public LotPerformanceNotFoundException(Long id) {
        super("Lot performance with id " + id + " not found");
    }

    public LotPerformanceNotFoundException(String message) {
        super(message);
    }
}
