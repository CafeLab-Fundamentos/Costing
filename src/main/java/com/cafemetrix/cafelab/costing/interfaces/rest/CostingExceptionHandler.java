package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.exceptions.InvalidPerformanceCalculationException;
import com.cafemetrix.cafelab.costing.domain.exceptions.LotPerformanceNotFoundException;
import com.cafemetrix.cafelab.shared.interfaces.rest.resources.MessageResource;
import com.cafemetrix.cafelab.shared.interfaces.rest.support.CafeLabScopedExceptionHandlerSupport;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CostingController.class)
@Order(1)
public class CostingExceptionHandler extends CafeLabScopedExceptionHandlerSupport {

    @ExceptionHandler(LotPerformanceNotFoundException.class)
    public ResponseEntity<MessageResource> handleLotPerformanceNotFoundException(
            LotPerformanceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResource(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPerformanceCalculationException.class)
    public ResponseEntity<MessageResource> handleInvalidPerformanceCalculationException(
            InvalidPerformanceCalculationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResource(ex.getMessage()));
    }
}
