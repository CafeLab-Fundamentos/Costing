package com.cafemetrix.cafelab.costing.domain.exceptions;

public class BatchNotFoundException extends RuntimeException {
    public BatchNotFoundException(Long id) {
        super("Batch with id " + id + " was not found");
    }

    public BatchNotFoundException(String message) {
        super(message);
    }
}
