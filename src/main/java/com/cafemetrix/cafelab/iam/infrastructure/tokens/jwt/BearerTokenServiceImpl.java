package com.cafemetrix.cafelab.iam.infrastructure.tokens.jwt;

import com.cafemetrix.cafelab.iam.application.internal.outboundservices.tokens.BearerTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Valida tokens HS256 firmados con el mismo secret que el microservicio IAM del monolitico.
 * Subject del JWT = email del usuario (convencion del monolitico).
 */
@Service
public class BearerTokenServiceImpl implements BearerTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenServiceImpl.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;

    public BearerTokenServiceImpl(@Value("${authorization.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            LOGGER.debug("Token JWT invalido: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
