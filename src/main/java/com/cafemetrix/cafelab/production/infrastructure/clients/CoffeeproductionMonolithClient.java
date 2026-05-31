package com.cafemetrix.cafelab.production.infrastructure.clients;

import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeLotSummary;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeproductionContextFacade;
import com.cafemetrix.cafelab.shared.infrastructure.web.CurrentProfileIdResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

/**
 * Cliente HTTP del Anti-Corruption Layer hacia el bounded context Production,
 * que vive en el microservicio Management (URL en {@code management.base-url}).
 *
 * <p>Como Costing y Management viven detras del mismo API Gateway, la llamada
 * es service-to-service: NO hay validacion JWT en la comunicacion interna. Se
 * propaga {@code X-User-Id} (cuando esta presente en el request original) para
 * trazabilidad y para que Management pueda aplicar sus propias reglas si lo
 * decide. La pertenencia del lote la decide quien llama, comparando
 * {@link CoffeeLotSummary#getUserId()} contra el {@code currentUserId}.</p>
 *
 * <p>El endpoint consumido es {@code GET /api/v1/coffee-lots/{id}} y se acepta
 * tanto camelCase ({@code userId/id}) como snake_case ({@code user_id/coffee_lot_id})
 * en la respuesta JSON.</p>
 */
@Service
public class CoffeeproductionMonolithClient implements CoffeeproductionContextFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeproductionMonolithClient.class);

    private final RestClient restClient;

    public CoffeeproductionMonolithClient(@Value("${management.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Optional<CoffeeLotSummary> getCoffeeLotById(Long coffeeLotId) {
        if (coffeeLotId == null) return Optional.empty();
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/v1/coffee-lots/{id}", coffeeLotId)
                    .headers(headers -> currentUserIdHeader()
                            .ifPresent(v -> headers.set(CurrentProfileIdResolver.USER_ID_HEADER, v)))
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, resp) -> {
                        // Lote inexistente: lo tratamos como "no accesible" (no excepcion).
                    })
                    .onStatus(status -> status.value() == 401 || status.value() == 403, (req, resp) -> {
                        LOGGER.warn("Management rechazo {} para lote {} (sin permisos)",
                                resp.getStatusCode(), coffeeLotId);
                    })
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
            if (body == null) return Optional.empty();

            Long id = readLong(body, "id", "coffee_lot_id", "coffeeLotId");
            Long userId = readLong(body, "userId", "user_id");

            if (id == null) {
                LOGGER.warn("Respuesta de Management para lote {} no trae 'id' ni 'coffee_lot_id'. Body: {}",
                        coffeeLotId, body.keySet());
                return Optional.empty();
            }
            return Optional.of(new CoffeeLotSummary(id, userId));
        } catch (Exception ex) {
            LOGGER.warn("Fallo al consultar lote {} en Management: {}", coffeeLotId, ex.getMessage());
            return Optional.empty();
        }
    }

    /** Lee el primer campo presente entre varios alias y lo convierte a Long. */
    private static Long readLong(Map<String, Object> body, String... fieldAliases) {
        for (String field : fieldAliases) {
            Object value = body.get(field);
            if (value != null) {
                try {
                    return Long.valueOf(value.toString().trim());
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Campo '{}' no es un Long valido: {}", field, value);
                }
            }
        }
        return null;
    }

    /** Devuelve el X-User-Id del request actual, si esta presente. */
    private static Optional<String> currentUserIdHeader() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) {
            return Optional.empty();
        }
        HttpServletRequest req = sra.getRequest();
        String header = req.getHeader(CurrentProfileIdResolver.USER_ID_HEADER);
        if (header == null || header.isBlank()) {
            header = req.getHeader(CurrentProfileIdResolver.USER_ID_HEADER_ALT);
        }
        return (header != null && !header.isBlank()) ? Optional.of(header) : Optional.empty();
    }
}
