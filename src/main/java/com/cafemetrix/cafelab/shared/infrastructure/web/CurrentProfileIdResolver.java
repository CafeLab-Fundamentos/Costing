package com.cafemetrix.cafelab.shared.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Lee el id de perfil que el API Gateway inyecta en el request tras validar al usuario.
 * Este microservicio no valida tokens; solo consume el header de confianza del gateway.
 */
@Component
public class CurrentProfileIdResolver implements ProfileIdResolver {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ID_HEADER_ALT = "X-Profile-Id";

    @Override
    public Optional<Long> resolveProfileId() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return Optional.empty();
        }
        HttpServletRequest req = sra.getRequest();
        String value = firstNonBlank(req.getHeader(USER_ID_HEADER), req.getHeader(USER_ID_HEADER_ALT));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
