package com.cafemetrix.cafelab.iam.application.internal.outboundservices.tokens;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Operaciones de validacion de JWT emitidos por el microservicio IAM del monolitico.
 * Costing solo VALIDA tokens; nunca los emite.
 */
public interface BearerTokenService {

    String getBearerTokenFrom(HttpServletRequest request);

    boolean validateToken(String token);

    String getUsernameFromToken(String token);
}
