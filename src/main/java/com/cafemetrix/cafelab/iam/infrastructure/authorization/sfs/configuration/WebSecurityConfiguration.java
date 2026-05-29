package com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.configuration;

import com.cafemetrix.cafelab.iam.application.internal.outboundservices.tokens.BearerTokenService;
import com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion de seguridad alineada con el monolitico:
 * - Stateless, CSRF off, CORS abierto a entornos esperados.
 * - Swagger y /error abiertos; cualquier otro request exige JWT valido.
 * - Costing NO emite tokens: el {@link BearerAuthorizationRequestFilter} solo valida.
 */
@Configuration
public class WebSecurityConfiguration {

    private final BearerTokenService tokenService;
    private final AuthenticationEntryPoint unauthorizedRequestHandler;

    public WebSecurityConfiguration(BearerTokenService tokenService,
                                    AuthenticationEntryPoint unauthorizedRequestHandler) {
        this.tokenService = tokenService;
        this.unauthorizedRequestHandler = unauthorizedRequestHandler;
    }

    @Bean
    public BearerAuthorizationRequestFilter authorizationRequestFilter() {
        return new BearerAuthorizationRequestFilter(tokenService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(unauthorizedRequestHandler))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/error").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://localhost:*",
                "https://127.0.0.1:*",
                "https://*.vercel.app",
                "https://*.web.app",
                "https://*.firebaseapp.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.ORIGIN,
                "X-Requested-With"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
