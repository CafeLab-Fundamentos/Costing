package com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.support;

import com.cafemetrix.cafelab.iam.infrastructure.clients.ProfileMonolithClient;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resuelve {@code profiles.id} (Long) del usuario autenticado.
 * Costing solo tiene el email del JWT como principal; el id real lo aporta el monolitico
 * via {@link ProfileMonolithClient}.
 */
@Component
public class CurrentProfileIdResolver {

    private final ProfileMonolithClient profileMonolithClient;

    public CurrentProfileIdResolver(ProfileMonolithClient profileMonolithClient) {
        this.profileMonolithClient = profileMonolithClient;
    }

    public Optional<Long> resolveProfileId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof String email) || email.isBlank()) {
            return Optional.empty();
        }
        return profileMonolithClient.findProfileIdByEmail(email);
    }
}
