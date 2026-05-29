package com.cafemetrix.cafelab.production.infrastructure.clients;

import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeLotSummary;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeproductionContextFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

/**
 * Implementacion HTTP del facade. Llama a {@code GET /api/v1/coffee-lots/{id}} del monolitico
 * reenviando el Bearer token del request actual. Si el monolitico responde 200 entonces el lote
 * existe y el usuario tiene acceso; si responde 403/404 se devuelve {@link Optional#empty()}.
 */
@Service
public class CoffeeproductionMonolithClient implements CoffeeproductionContextFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeproductionMonolithClient.class);

    private final RestClient restClient;

    public CoffeeproductionMonolithClient(@Value("${monolith.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Optional<CoffeeLotSummary> getCoffeeLotById(Long coffeeLotId) {
        if (coffeeLotId == null) return Optional.empty();
        String bearer = currentBearerToken();
        if (bearer == null) {
            LOGGER.debug("Sin Authorization en el request actual; no se puede consultar el lote {}", coffeeLotId);
            return Optional.empty();
        }
        try {
            Map<String, Object> body = restClient.get()
                    .uri("/api/v1/coffee-lots/{id}", coffeeLotId)
                    .header(HttpHeaders.AUTHORIZATION, bearer)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        // 401/403/404 -> el monolitico ya valido y rechazo; tratamos como "no accesible"
                    })
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
            if (body == null || body.get("id") == null) return Optional.empty();
            Long id = Long.valueOf(body.get("id").toString());
            Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
            return Optional.of(new CoffeeLotSummary(id, userId));
        } catch (Exception ex) {
            LOGGER.warn("Fallo al consultar lote {} en el monolitico: {}", coffeeLotId, ex.getMessage());
            return Optional.empty();
        }
    }

    private String currentBearerToken() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) return null;
        HttpServletRequest req = sra.getRequest();
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        return (header != null && header.startsWith("Bearer ")) ? header : null;
    }
}
