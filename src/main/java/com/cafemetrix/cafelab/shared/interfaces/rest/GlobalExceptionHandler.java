package com.cafemetrix.cafelab.shared.interfaces.rest;


import com.cafemetrix.cafelab.shared.interfaces.rest.resources.MessageResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized REST exception handling (aligned with MediTrack {@code GlobalExceptionHandler} structure).
 *
 * <p>Bean validation errors return a structured body with {@code errors: [{ field, message }]} for the
 * frontend. {@link IllegalArgumentException} returns {@link MessageResource} to match existing API clients.</p>
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> items = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("field", fe.getField());
            String msg = fe.getDefaultMessage();
            row.put("message", msg != null && !msg.isBlank() ? msg : "Valor inválido");
            items.add(row);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Error de validación");
        body.put("errors", items);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Fallback when no bounded-context advice applies (e.g. non-REST beans).
     * Context-scoped advices use {@link com.cafemetrix.cafelab.shared.interfaces.rest.support.CafeLabScopedExceptionHandlerSupport}.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResource> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "Solicitud inválida";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResource(msg));
    }
}
