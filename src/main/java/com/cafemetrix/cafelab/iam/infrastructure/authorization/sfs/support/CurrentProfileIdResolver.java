package com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Resuelve {@code profiles.id} del usuario que origino el request.
 *
 * <p>El microservicio Costing vive detras de un API Gateway que valida JWT
 * y reenvia la peticion inyectando el id del perfil resuelto en el header
 * {@value #USER_ID_HEADER}. Este componente solo lee ese header; NO valida
 * tokens ni habla con el monolito.</p>
 *
 * <p>Para soportar pruebas locales sin API Gateway tambien se acepta el
 * header alternativo {@value #USER_ID_HEADER_ALT} (mas comun en mock tools).</p>
 */
@Component
public class CurrentProfileIdResolver {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ID_HEADER_ALT = "X-Profile-Id";

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
