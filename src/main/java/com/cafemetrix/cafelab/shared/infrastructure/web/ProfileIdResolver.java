package com.cafemetrix.cafelab.shared.infrastructure.web;

import java.util.Optional;

/**
 * Resuelve el id de perfil inyectado por el API Gateway en el request HTTP.
 */
public interface ProfileIdResolver {

    Optional<Long> resolveProfileId();
}
