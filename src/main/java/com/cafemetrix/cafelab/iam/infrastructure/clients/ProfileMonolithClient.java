package com.cafemetrix.cafelab.iam.infrastructure.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cliente HTTP que resuelve el {@code profiles.id} (Long) del monolitico a partir del email
 * del JWT. El monolitico expone {@code GET /api/v1/profiles?email=...} sin requerir auth.
 *
 * <p>La pareja email -> id es estable, asi que se cachea en memoria para no golpear el monolitico
 * en cada request.</p>
 */
@Component
public class ProfileMonolithClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileMonolithClient.class);

    private final RestClient restClient;
    private final ConcurrentHashMap<String, Long> emailToProfileId = new ConcurrentHashMap<>();

    public ProfileMonolithClient(@Value("${monolith.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Optional<Long> findProfileIdByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        Long cached = emailToProfileId.get(email);
        if (cached != null) return Optional.of(cached);
        try {
            List<Map<String, Object>> body = restClient.get()
                    .uri(uri -> uri.path("/api/v1/profiles").queryParam("email", email).build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
            if (body == null || body.isEmpty()) return Optional.empty();
            Object rawId = body.get(0).get("id");
            if (rawId == null) return Optional.empty();
            Long id = Long.valueOf(rawId.toString());
            emailToProfileId.put(email, id);
            return Optional.of(id);
        } catch (Exception ex) {
            LOGGER.warn("No se pudo resolver perfil por email={} contra el monolitico: {}", email, ex.getMessage());
            return Optional.empty();
        }
    }
}
