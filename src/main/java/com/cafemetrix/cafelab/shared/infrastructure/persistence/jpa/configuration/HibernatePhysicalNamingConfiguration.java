package com.cafemetrix.cafelab.shared.infrastructure.persistence.jpa.configuration;

import com.cafemetrix.cafelab.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra la {@link PhysicalNamingStrategy} como bean para que Spring Boot la inyecte en Hibernate
 * sin resolver el FQN por nombre (evita fallos con DevTools / classloaders del IDE).
 */
@Configuration
public class HibernatePhysicalNamingConfiguration {

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();
    }
}
