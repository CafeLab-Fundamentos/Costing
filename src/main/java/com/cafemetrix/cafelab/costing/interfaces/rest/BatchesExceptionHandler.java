package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.exceptions.BatchNotFoundException;
import com.cafemetrix.cafelab.shared.interfaces.rest.resources.MessageResource;
import com.cafemetrix.cafelab.shared.interfaces.rest.support.CafeLabScopedExceptionHandlerSupport;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = BatchesController.class)
@Order(1)
public class BatchesExceptionHandler extends CafeLabScopedExceptionHandlerSupport {

    @ExceptionHandler(BatchNotFoundException.class)
    public ResponseEntity<MessageResource> handleBatchNotFound(BatchNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResource(ex.getMessage()));
    }

    @ExceptionHandler(BatchesController.ForbiddenBatchAccessException.class)
    public ResponseEntity<MessageResource> handleForbidden(BatchesController.ForbiddenBatchAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResource(ex.getMessage()));
    }
}
