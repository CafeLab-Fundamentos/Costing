package com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.pipeline;

import com.cafemetrix.cafelab.iam.application.internal.outboundservices.tokens.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro alineado con el monolitico: valida JWT (HS256, subject=email) y deja el email
 * como Principal en el SecurityContext. Costing no tiene tabla de usuarios, por lo que
 * NO carga UserDetails desde BD: confia en el sujeto firmado por IAM del monolitico.
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);

    private final BearerTokenService tokenService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            if (path.contains("/swagger-")
                    || path.startsWith("/swagger-ui")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/webjars/")
                    || path.equals("/error")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = tokenService.getBearerTokenFrom(request);
            if (token != null && tokenService.validateToken(token)) {
                String email = tokenService.getUsernameFromToken(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            LOGGER.error("No se pudo establecer la autenticacion del usuario", ex);
        }
        filterChain.doFilter(request, response);
    }
}
